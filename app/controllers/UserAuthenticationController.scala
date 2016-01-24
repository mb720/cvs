package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignInForm
import forms.SignInForm.Data
import models.CvsUser
import modules.SilhouetteModule
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, AnyContent, Request}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Responsible for signing in and out users.
  * Created by Matthias Braun on 21.12.2015.
  */
class UserAuthenticationController @Inject()(val messagesApi: MessagesApi) extends Silhouette[CvsUser, CookieAuthenticator] with SilhouetteModule {

  def handleLoginData(data: Data)(implicit request: Request[AnyContent]) = {
    Logger.info("Handling login data")
    if (request.secure) {
      val credentials = Credentials(data.email, data.password)
      credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
        val redirectHereAfterSignIn = Redirect(routes.Application.index())
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            Logger.info(s"Found user with matching login info: ${user.fullName}")
            env.authenticatorService.create(loginInfo).map {
              case authenticator => authenticator
            }.flatMap { authenticator =>
              // The authenticator service is configured in the AuthenticatorServiceModule
              Logger.info("Initializing authenticator")
              env.authenticatorService.init(authenticator).flatMap { value =>
                Logger.info("Embedding cookie into response")
                env.authenticatorService.embed(value, redirectHereAfterSignIn).onComplete {
                  case Success(authenticatorResult) =>
                    Future.successful(authenticatorResult)
                  case Failure(ex) =>
                    Logger.info(s"Could not embed authenticator value into response: $ex")
                    Future.failed(ex)
                }
                env.authenticatorService.embed(value, redirectHereAfterSignIn)
              }.recover {
                case ex =>
                  Logger.warn(s"Could not initialize authenticator: $ex")
                  Redirect(routes.UserAuthenticationController.showSignInForm()).flashing("error" -> Messages("authentication.error"))
              }
            }.recover {
              case ex =>
                Logger.warn(s"Something went wrong while authenticating the user: $ex")
                Redirect(routes.UserAuthenticationController.showSignInForm()).flashing("error" -> Messages("invalid.credentials"))
            }
          case None => Logger.warn("Could not find user"); Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }.recover {
        case ex: ProviderException =>
          Logger.info(s"Invalid credentials: $ex")
          Redirect(routes.UserAuthenticationController.showSignInForm()).flashing("error" -> Messages("invalid.credentials"))
      }
    } else Future.successful(Forbidden(Messages("error.request.not.secure")))
  }


  /**
    * When a user provides their credentials in order to sign in, this method checks that the credentials are valid.
    *
    * @return The result to display.
    */
  def authenticate = Action.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(form => Future.successful(BadRequest("Bad request")), data => handleLoginData(data))
  }

  /**
    * Signs out the current user.
    *
    * @return the result of the page to which we redirect the signed out user
    */
  def signOut = SecuredAction.async { implicit request =>
    val result = Redirect(routes.Application.index()).flashing("success" -> Messages("sign.out.success"))

    env.authenticatorService.discard(request.authenticator, result)
  }

  /**
    * Shows the sign in form.
    */
  def showSignInForm = UserAwareAction.async { implicit request =>
    if (request.secure) {
      Logger.info("Show sign in page")
      makeSureThereIsAdmin()
      request.identity match {
        case Some(user) => Future.successful(Ok(s"You are already signed in, ${user.firstName}"))
        case None => Future.successful {
          Logger.info("There's nobody signed in yet")
          Ok(views.html.signIn(SignInForm.form))
        }
      }
    } else {
      Future.successful(Forbidden(Messages("error.request.not.secure")))
    }
  }

  def makeSureThereIsAdmin() = {
    Logger.info("Making sure there is an admin user")
    val adminLoginInfo = LoginInfo(CredentialsProvider.ID, "admin@nowhere.com")
    userService.retrieve(adminLoginInfo).flatMap {
      case Some(admin) => Future.successful(Logger.info("Admin already exists"))
      case None => Future.successful {
        val email = adminLoginInfo.providerKey
        userService.save(createAdmin(email), adminLoginInfo)
        val passHash = passwordHasher.hash("ezgportal")
        authInfoRepo.add(adminLoginInfo, passHash)
      }
    }
  }

  def createAdmin(email: String): CvsUser = {
    Logger.info("Creating admin")
    CvsUser(
      ID = UUID.randomUUID(),
      firstName = "Ad",
      lastName = "Min",
      email = email
    )
  }
}
