package models.daos.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.CvsUser
import models.daos.logininfo.LoginInfoDao

import scala.concurrent.Future

/**
  * Lets us save and retrieve users.
  */
trait CvsUserDao {
  def loginInfoDao: LoginInfoDao

  def save(user: CvsUser, loginInfo: LoginInfo)

  /**
    * Finds a user by their login info.
    *
    * @param loginInfo the login info of the user to find
    * @return the found user or None if no user for the given login info could be found
    */
  def retrieve(loginInfo: LoginInfo): Future[Option[CvsUser]]

  /**
    * Finds a user by their user ID.
    *
    * @param userID the ID of the user to find
    * @return the found user or None if no user for the given ID could be found
    */
  def retrieve(userID: UUID): Future[Option[CvsUser]]

}
