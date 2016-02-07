package controllers

import models.daos.CvsProductDAO
import models.tables.CvsProduct
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Matthias Braun
 */
class CvsProducts @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {
  val productForm = Form(
    mapping(
        "id" -> longNumber,
        "name" -> nonEmptyText
      )
     // Create a product with the given ID and name using the companion object
      (CvsProduct.apply)(CvsProduct.unapply)
  )

  def showProductForm = Action { implicit request =>
    Ok(views.html.product.form(productForm))
  }

  def handleNewProduct = Action { implicit request =>
    productForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.product.form(formWithErrors))
      },
      newProduct => {
        CvsProductDAO.insert(newProduct)
        val successMsg = s"${newProduct.name} saved successfully"
        Redirect(routes.CvsProducts.showProductForm()).flashing("success" -> successMsg)
      }
    )
  }

  def listProducts = Action.async {
    val futureProductNames = CvsProductDAO.getAll.map(_.map(_.name))
    val futureProductNamesCombined = futureProductNames.map(_.mkString("\n"))
    futureProductNamesCombined.map(msgs => Ok(s"Listing products:\n$msgs"))
  }
}
