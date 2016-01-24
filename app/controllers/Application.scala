package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.CvsUser
import modules.SilhouetteModule
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc._
import views._

class Application @Inject()(val messagesApi: MessagesApi) extends Silhouette[CvsUser, CookieAuthenticator] with SilhouetteModule {

  val siteTitle = "CVS"

  val helloForm = Form(
    tuple(
      "name" -> nonEmptyText,
      "repeat" -> number(min = 1, max = 100),
      "color" -> optional(text)
    )
  )

  def index = UserAwareAction { implicit request =>
    if (request.secure) Ok(html.index(siteTitle, helloForm, request.identity)) else
    Forbidden(Messages("error.request.not.secure"))

  }

  def sayHello = Action { implicit request =>
    helloForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.index(siteTitle, formWithErrors, None)), { case (name, repeat, color) => Ok(html.result(name, repeat.toInt, color)) }
    )
  }
}
