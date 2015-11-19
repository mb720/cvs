package controllers

import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc._
import javax.inject.Inject
import java.io.File

class FileUpload @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def uploadForm = Action {
    Ok(views.html.uploadFile())
  }

  def handleUpload = Action(parse.multipartFormData) { request =>
    request.body.file("uploadedFile") match {

      case Some(file) =>
        val fileName = file.filename
        val uploadDir = "C:/Users/marakai/Desktop/uploaded/"
        file.ref.moveTo(new File(uploadDir + fileName))
        Ok(s"File uploaded: $fileName")

      case None =>
        Redirect(routes.FileUpload.uploadForm()).flashing(
          "error" -> "Invalid file")
    }
  }
}
