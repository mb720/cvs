package models

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity

/**
  * The user object.
  *
  * @param ID    the unique ID of the user
  * @param name  the user's name
  * @param email the user's email
  */
case class CvsUser(ID: UUID,
                   name: String,
                   email: String
                  ) extends Identity {
}

