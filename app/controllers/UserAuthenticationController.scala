package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import play.api.mvc.Action
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasher}
import com.mohiva.play.silhouette.api.{Environment, LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignInForm
import models.CvsUser
import models.services.UserService
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
  * Responsible for signing in and out users.
  * Created by Matthias Braun on 21.12.2015.
  */
class UserAuthenticationController @Inject()(val messagesApi: MessagesApi, val env: Environment[CvsUser, CookieAuthenticator],
                                             userService: UserService, authInfoRepo: AuthInfoRepository,
                                             passwordHasher: PasswordHasher, credentialsProvider: CredentialsProvider)
  extends Silhouette[CvsUser, CookieAuthenticator] {

  def createAdmin(adminLoginInfo: LoginInfo): CvsUser = {
    Logger.info("Creating admin")
    CvsUser(
      userID = UUID.randomUUID(),
      loginInfo = adminLoginInfo,
      firstName = "Ad",
      lastName = "Min",
      fullName = "Admin",
      email = adminLoginInfo.providerKey
    )
  }

  /**
    * When a user provides their credentials in order to sign in, this method checks that the credentials are valid.
    *
    * @return The result to display.
    */
  def authenticate = Action.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest("Bad request")),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          val result = Redirect(routes.Application.index())
          userService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              Logger.info(s"User authenticated: ${user.fullName}")
              env.authenticatorService.create(loginInfo).map {
                case authenticator => authenticator
              }.flatMap { authenticator =>
                // If we use a CookieAuthenticator, we need to configure it in conf/silhouette.conf
                env.authenticatorService.init(authenticator).flatMap { value =>
                  env.authenticatorService.embed(value, result)
                }
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case e: ProviderException =>
            Logger.info("Invalid credentials")
            Redirect(routes.UserAuthenticationController.showSignInForm()).flashing("error" -> Messages("invalid.credentials"))
        }
      }
    )
  }
  /**
    * Signs out the current user.
    *
    * @return the result of the page to which we redirect the signed out user
    */
  def signOut = SecuredAction.async { implicit request =>
    val result = Redirect(routes.Application.index())
    env.authenticatorService.discard(request.authenticator, result)
  }

  def makeSureThereIsAdmin() = {
    Logger.info("Making sure there is an admin user")
    val adminLoginInfo = LoginInfo(CredentialsProvider.ID, "admin@nowhere.com")
    userService.retrieve(adminLoginInfo).flatMap {
      case Some(admin) => Future.successful(Logger.info("Admin already exists"))
      case None => Future.successful {
        userService.save(createAdmin(adminLoginInfo))
        val passHash = passwordHasher.hash("ezgportal")
        authInfoRepo.add(adminLoginInfo, passHash)
      }
    }
  }

  /**
    * Shows the sign in form.
    */
  def showSignInForm = UserAwareAction.async { implicit request =>
    Logger.info("Show sign in page")
    makeSureThereIsAdmin()
    request.identity match {
      case Some(user) => Future.successful(Ok(s"You are already signed in, ${user.firstName}"))
      case None => Future.successful {
        Logger.info("There's nobody signed in yet")
        Ok(views.html.signIn(SignInForm.form))
      }
    }
  }
}
