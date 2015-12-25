package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.SignInForm
import models.CvsUser
import models.services.UserService
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action

import scala.concurrent.Future
import scala.language.postfixOps

/**
  * The credentials auth controller.
  *
  * @param messagesApi The Play messages API.
  * @param env The Silhouette environment.
  * @param userService The user service implementation.
  * @param credentialsProvider The credentials provider.
  */
class CredentialsAuthController @Inject()(val messagesApi: MessagesApi,
                                          val env: Environment[CvsUser, CookieAuthenticator],
                                          userService: UserService,
                                          credentialsProvider: CredentialsProvider
                                         )
  extends Silhouette[CvsUser, CookieAuthenticator] {

  /**
    * Authenticates a user against the credentials provider.
    *
    * @return The result to display.
    */
  def authenticate = Action.async { implicit request =>
    Logger.info("Authenticating")
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest("Bad request")),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          Logger.info(s"Authentication was successful. Login info: $loginInfo")
          val result = Redirect(routes.Application.index())
          userService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              Logger.info(s"Got user: $user")
              env.authenticatorService.create(loginInfo).map {
                case authenticator => authenticator
              }.flatMap { authenticator =>
                env.eventBus.publish(LoginEvent(user, request, request2Messages))
                env.authenticatorService.init(authenticator).flatMap { value =>
                  Logger.info(s"Embedding value into response: ${value.name}")
                  env.authenticatorService.embed(value, result)
                }
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case e: ProviderException =>
            Logger.info("Invalid credentials")
            Redirect(routes.SignInController.signIn()).flashing("error" -> Messages("invalid.credentials"))
        }
      }
    )
  }
}
