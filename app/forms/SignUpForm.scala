package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

/**
  * Represents the sign up form and its data.
  * Created by Matthias Braun on 2/7/2016.
  */
object SignUpForm {

  /**
    * Maps the fields in the HTML form to a Scala object.
    */
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText.verifying(minLength(10))
    )(SignUpData.apply)(SignUpData.unapply)
  )

  /**
    * The sign up form data.
    *
    * @param name     the user's name
    * @param email    email of the user
    * @param password password of the user
    */
  case class SignUpData(name: String, email: String, password: String)

}
