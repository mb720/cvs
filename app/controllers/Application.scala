package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.CvsUser
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.MessagesApi
import play.api.mvc._
import views._

class Application @Inject()(val messagesApi: MessagesApi, val env: Environment[CvsUser, CookieAuthenticator])
  extends Silhouette[CvsUser, CookieAuthenticator] {
  val siteTitle = "CVS"

  val bootstrapForm = Form(
    tuple(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "rememberMe" -> boolean
    ))

  val helloForm = Form(
    tuple(
      "name" -> nonEmptyText,
      "repeat" -> number(min = 1, max = 100),
      "color" -> optional(text)
    )
  )

  def index = UserAwareAction { implicit request =>
    val userName = request.identity match {
      case Some(identity) => identity.fullName
      case None => "Guest"
    }
    Logger.info(s"Hi, $userName")
    Ok(html.index(siteTitle, helloForm, bootstrapForm))
  }

  def sayHello = Action {
    implicit request =>
      helloForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.index(siteTitle, formWithErrors, bootstrapForm)), { case (name, repeat, color) => Ok(html.result(name, repeat.toInt, color)) }
      )
  }

  def showBootstrapFormContents() = Action {
    implicit request =>
      bootstrapForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.index(siteTitle, helloForm, formWithErrors)), { case (userEmail, password, remember) =>
          val userInputAsMap = Map("Email" -> userEmail, "Password" -> password, "Remember me" -> String.valueOf(remember))
          Ok("...")
        }
      )
  }

}
