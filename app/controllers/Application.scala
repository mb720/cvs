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
import utils.MarkdownTransformer
import views._

class Application @Inject()(val messagesApi: MessagesApi) extends Silhouette[CvsUser, CookieAuthenticator] with SilhouetteModule {

  val siteTitle = "CVS"

  val markdownForm = Form(
    "markdownText" -> text
  )

  def index = UserAwareAction { implicit request =>
    if (request.secure) Ok(html.index(siteTitle, markdownForm, request.identity))
    else Forbidden(Messages("error.request.not.secure"))

  }

  def testMd = Action { implicit request =>
    markdownForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.index(siteTitle, formWithErrors, None)), { case (markdown) => Ok(s"<html>${MarkdownTransformer.transform(markdown)}</html>").as("text/html") }
    )
  }
}
