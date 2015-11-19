package controllers

import models.HarvestGuess
import play.api.data.Form
import play.api.i18n.{MessagesApi, I18nSupport}
import javax.inject.Inject

import play.api.mvc.{Action, Controller}

import play.api.data.Forms._
import views.html

class HarvestGuessForm @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val siteTitle = "Erntemeldung"

  val form = Form(
    mapping(
      "id" -> longNumber,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "address" -> nonEmptyText
    )
      // Create a harvest guess with the given data using the companion object
      (HarvestGuess.apply)(HarvestGuess.unapply)
  )

  def showForm() = Action {
    Ok(html.harvest.harvestGuess(siteTitle, form))

  }

  def showHarvestGuessFormContents() = Action {
    Ok("Harvest guess form was filled out")
  }

}