package models

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity

/**
  * The user object.
  *
  * @param ID the unique ID of the user
  * @param firstName the first name of the authenticated user
  * @param lastName the last name of the authenticated user
  * @param email the email of the authenticated provider
  */
case class CvsUser(ID: UUID,
                   firstName: String,
                   lastName: String,
                   email: String
                  ) extends Identity {

  def fullName: String = firstName + " " + lastName
}

