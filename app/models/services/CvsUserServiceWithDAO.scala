package models.services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import models.CvsUser
import models.daos.user.CvsUserDao

import scala.concurrent.Future

/**
  * Handles actions to users.
  *
  * @param userDAO the user DAO implementation
  */
class CvsUserServiceWithDao (userDAO: CvsUserDao) extends CvsUserService {

  override def save(user: CvsUser, loginInfo: LoginInfo) = {
    userDAO.save(user, loginInfo)
  }

  /**
    * Retrieves a user that matches the specified login info.
    *
    * @param loginInfo The login info to retrieve a user.
    * @return The retrieved user or None if no user could be retrieved for the given login info.
    */
  def retrieve(loginInfo: LoginInfo): Future[Option[CvsUser]] = userDAO.retrieve(loginInfo)

}
