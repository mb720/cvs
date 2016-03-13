package controllers

import javax.inject.Inject

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import utils.MarkdownTransformer

/**
  * Created by Matthias Braun on 2/14/2016.
  */

class MarkdownController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def transformMarkdown = Action(parse.text) { implicit request =>
    Logger.info(s"Got markdown: ${request.body}")
    Ok(MarkdownTransformer.transform(request.body))
  }
}
