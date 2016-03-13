package utils


import laika.api.Transform
import laika.parse.markdown.Markdown

import scala.io.Codec

/**
  * Created by Matthias Braun on 2/14/2016.
  */
object MarkdownTransformer {

  def transform(parseThis: String): String = {
    implicit val codec = Codec.UTF8
    import laika.render.HTML
    val transformOperation = Transform from Markdown to HTML fromString parseThis

    transformOperation.toString()
  }

}
