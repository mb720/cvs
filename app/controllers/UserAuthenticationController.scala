package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignInForm.SignInData
import forms.SignUpForm.SignUpData
import forms.{SignInForm, SignUpForm}
import models.CvsUser
import modules.SilhouetteModule
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, AnyContent, Request, Result}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Responsible for signing in, up, and out users.
  * Created by Matthias Braun on 21.12.2015.
  */
class UserAuthenticationController @Inject()(val messagesApi: MessagesApi) extends Silhouette[CvsUser, CookieAuthenticator] with SilhouetteModule {

  def handleLoginData(data: SignInData)(implicit request: Request[AnyContent]) = {
    Logger.info("Handling login data")
    if (request.secure) {
      val credentials = Credentials(data.email, data.password)
      credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
        val redirectHereAfterSignIn = Redirect(routes.Application.index())
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            Logger.info(s"Found user with matching login info: ${user.name}")
            env.authenticatorService.create(loginInfo).map {
              case authenticator => authenticator
            }.flatMap { authenticator =>
              // The authenticator service is configured in the AuthenticatorServiceModule
              env.authenticatorService.init(authenticator).flatMap { value =>
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


  def signUp = Action.async(implicit request =>
    SignUpForm.form.bindFromRequest.fold(form => Future.successful(BadRequest("Bad request")), data => signUpUser(data))
  )

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
    * Shows the sign up form.
    */
  def showSignUpForm = Action { implicit request =>
    if (request.secure) {
      Ok(views.html.signUp(SignUpForm.form))
    } else {
      Forbidden(Messages("error.request.not.secure"))
    }
  }

  /**
    * Shows the sign in form.
    */
  def showSignInForm = UserAwareAction.async { implicit request =>
    if (request.secure) {
      makeSureThereIsAdmin()
      request.identity match {
        case Some(user) => Future.successful(Ok(s"You are already signed in, ${user.name}"))
        case None => Future.successful {
          Logger.info("There's nobody signed in yet")
          Ok(views.html.signIn(SignInForm.form))
        }
      }
    } else {
      Future.successful(Forbidden(Messages("error.request.not.secure")))
    }
  }

  def createUser(userData: SignUpData) = CvsUser(
    ID = UUID.randomUUID(),
    name = userData.name,
    email = userData.email
  )

  def signUpUser(userData: SignUpData): Future[Result] = {
    if (userData == null) {
      Future.successful(InternalServerError("Can't sign up null user"))
    } else {
      signUpUser(createUser(userData), userData.password)
    }
  }

  def signUpUser(user: CvsUser, password: String): Future[Result] = {
    Logger.info("Signing up user")
    val userEmail = user.email
    val userLoginInfo = LoginInfo(CredentialsProvider.ID, userEmail)
    val passHash = passwordHasher.hash(password)
    userService.retrieve(userLoginInfo).map {
      case Some(preexistingUser) =>
        Logger.info(s"User with mail $userEmail already exists")
        Ok("User with that mail already exists")
      case None =>
        userService.save(user, userLoginInfo)
        Logger.info("Adding user login info and password to repo")
        authInfoRepo.add(userLoginInfo, passHash)
        Redirect(routes.Application.index()).flashing("success" -> Messages("sign.up.success"))
    }.recover { case t: Throwable => InternalServerError(s"Could not retrieve user with mail $userEmail") }
  }

  def makeSureThereIsAdmin() = {
    Logger.info("Making sure there is an admin user")
    val adminEmail = "admin@nowhere.com"
    val adminLoginInfo = LoginInfo(CredentialsProvider.ID, adminEmail)
    userService.retrieve(adminLoginInfo).map {
      case Some(admin) =>
        Logger.info("Admin already exists")
      case None =>
        val email = adminLoginInfo.providerKey
        val admin = CvsUser(UUID.randomUUID(), "Admin", email)
        userService.save(admin, adminLoginInfo)
        val passHash = passwordHasher.hash("ezgportal")
        Logger.info("Adding admin login info and password to repo")
        authInfoRepo.add(adminLoginInfo, passHash)
    }.recover { case t: Throwable => Logger.warn("Could not access user service") }
  }
}
