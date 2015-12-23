package controllers

import javax.inject.Inject

import forms.data.{FarmerPersonalData, HarvestGuessFromForm}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import views.html

class HarvestGuessFormController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val siteTitle = "Erntemeldung"

  val form = Form(
    mapping(
      "contactData" -> mapping(
        "name" -> nonEmptyText,
        "postalAddress" -> nonEmptyText,
        "phoneNumber" -> nonEmptyText,
        "emailAddress" -> nonEmptyText
      )(FarmerPersonalData.apply)(FarmerPersonalData.unapply)
    )(HarvestGuessFromForm.apply)(HarvestGuessFromForm.unapply)
  )

  //
  // Put the data the user has entered in the form into an object using its companion object
  def showForm() = Action {
    Logger.info("Showing harvest guess")
    Ok(html.harvest.harvestGuess(siteTitle, form))
  }

  def handleHarvestGuess() = Action { implicit request =>
    Logger.info("Handling harvest guess")
    //Ok("Harvest guess form was filled out")
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.info("Harvest guess has errors: " + formWithErrors.errors)
        BadRequest(views.html.harvest.harvestGuess(siteTitle, formWithErrors))
      },
      harvestGuessFromForm => {
        Logger.info("Harvest guess received: " + harvestGuessFromForm)
        Redirect(routes.HarvestGuessFormController.harvestGuessPosted())
      }
    )
  }

  def harvestGuessPosted() = Action {
    Ok(html.harvest.harvestGuessCompleted(siteTitle))
  }
}
