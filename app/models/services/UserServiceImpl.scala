package models.services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.CvsUser
import models.daos.UserDAO
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
  * Handles actions to users.
  *
  * @param userDAO The user DAO implementation.
  */
class UserServiceImpl @Inject()(userDAO: UserDAO) extends UserService {

  val DefaultFirstName = "Unknown first name"
  val DefaultLastName = "Unknown last name"
  val DefaultFullName = "Unknown full name"
  val DefaultEmail = "unknown@unknown.com"

  /**
    * Retrieves a user that matches the specified login info.
    *
    * @param loginInfo The login info to retrieve a user.
    * @return The retrieved user or None if no user could be retrieved for the given login info.
    */
  def retrieve(loginInfo: LoginInfo): Future[Option[CvsUser]] = userDAO.find(loginInfo)

  /**
    * Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  def save(user: CvsUser) = userDAO.save(user)

  /**
    * Saves the social profile for a user.
    *
    * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
    *
    * @param profile the social profile to save
    * @return the user for whom the profile was saved
    */
  def save(profile: CommonSocialProfile) = {
    userDAO.find(profile.loginInfo).flatMap {
      // Update the user data according to the profile
      case Some(user) =>
        userDAO.save(user.copy(
          firstName = profile.firstName.getOrElse(DefaultFirstName),
          lastName = profile.lastName.getOrElse(DefaultLastName),
          fullName = profile.fullName.getOrElse(DefaultFullName),
          email = profile.email.getOrElse(DefaultEmail)
        ))
      // Add a new user
      case None =>
        userDAO.save(CvsUser(
          userID = UUID.randomUUID(),
          loginInfo = profile.loginInfo,
          firstName = profile.firstName.getOrElse(DefaultFirstName),
          lastName = profile.lastName.getOrElse(DefaultLastName),
          fullName = profile.fullName.getOrElse(DefaultFullName),
          email = profile.email.getOrElse(DefaultEmail)
        ))
    }
  }
}
