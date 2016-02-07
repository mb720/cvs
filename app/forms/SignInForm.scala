package forms

import play.api.data.Form
import play.api.data.Forms._

/**
  * Represents the sign in form and its data.
  */
object SignInForm {

  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(SignInData.apply)(SignInData.unapply)
  )

  /**
    * The sign in form data.
    *
    * @param email    email of the user
    * @param password password of the user
    */
  case class SignInData(email: String, password: String)

}
