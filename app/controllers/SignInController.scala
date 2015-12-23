package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import forms.SignInForm
import models.User
import play.api.data.Form
import play.api.i18n.MessagesApi

import play.api.data.Forms._
import scala.concurrent.Future

/**
  * Created by Matthias Braun on 21.12.2015.
  */
class SignInController @Inject()(val messagesApi: MessagesApi,
                                 val env: Environment[User, CookieAuthenticator] )
  extends Silhouette[User, CookieAuthenticator] {

  /**
    * Handles the Sign In action.
    *
    * @return The result to display.
    */
  def signIn = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Ok("You are successfully signed in"))
      case None => Future.successful(Ok(views.html.signIn(SignInForm.form)))
    }
  }
}
