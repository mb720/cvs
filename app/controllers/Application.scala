package controllers

import javax.inject.Inject

import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi }
import play.api.mvc._
import views._

class Application @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

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
  val stringPairForm = Form(
    tuple(
      "name" -> nonEmptyText,
      "other" -> nonEmptyText
    )
  )

  def index = Action {
    Ok(html.index(siteTitle, helloForm, bootstrapForm))
  }

  def sayHello = Action {
    implicit request =>
      helloForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.index(siteTitle, formWithErrors, bootstrapForm)), { case (name, repeat, color) => Ok(html.result(name, repeat.toInt, color)) }
      )
  }

  //  def listMsgs = Action.async {
  //    val futureMsgContent = DAO.getAll.map(_.map(_.content))
  //    val futureMsgsCombined = futureMsgContent.map(_.mkString("\n"))
  //    futureMsgsCombined.map(msgs => Ok(s"Listing messages:\n$msgs"))
  //  }

  //  def newMsg(content: String) = Action {
  //    Logger.debug(s"Storing this message in DB: $content")
  //    DAO.insert(Message(Option(1L), content))
  //    Redirect(routes.Application.listMsgs())
  //  }
  def test() = Action {
    Logger.info("testing")
    Ok(html.viewTests(stringPairForm, "Test"))
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
