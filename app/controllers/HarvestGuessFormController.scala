package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import forms.data.{FarmerPersonalData, HarvestGuessFromForm}
import models.CvsUser
import modules.EnvironmentModule
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import views.html

  class HarvestGuessFormController @Inject()(val messagesApi: MessagesApi) extends Silhouette[CvsUser, CookieAuthenticator] with EnvironmentModule{
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

  // Put the data the user has entered in the form into an object using its companion object
  def showForm = UserAwareAction { request =>
    Logger.info("Showing harvest guess")
    val user = request.identity
    Logger.info(s"User: $user")
    Ok(html.harvest.harvestGuess(siteTitle, form, user))
  }

  def handleHarvestGuess = UserAwareAction { implicit request =>
    val userMaybe= request.identity
    Logger.info("Handling harvest guess")
    //Ok("Harvest guess form was filled out")
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.info("Harvest guess has errors: " + formWithErrors.errors)
        BadRequest(views.html.harvest.harvestGuess(siteTitle, formWithErrors, userMaybe))
      },
      harvestGuessFromForm => {
        Logger.info("Harvest guess received: " + harvestGuessFromForm)
        Redirect(routes.HarvestGuessFormController.harvestGuessPosted())
      }
    )
  }

  def harvestGuessPosted() = UserAwareAction { request =>
    val user = request.identity
    Ok(html.harvest.harvestGuessCompleted(siteTitle, user ))
  }
}
