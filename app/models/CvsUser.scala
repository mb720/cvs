package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

/**
  * The user object.
  *
  * @param userID the unique ID of the user.
  * @param loginInfo the linked login info.
  * @param firstName the first name of the authenticated user.
  * @param lastName the last name of the authenticated user.
  * @param fullName the full name of the authenticated user.
  * @param email the email of the authenticated provider.
  */
case class CvsUser(
                    userID: UUID,
                    loginInfo: LoginInfo,
                    firstName: String,
                    lastName: String,
                    fullName: String,
                    email: String
                  ) extends Identity
