package models.daos.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.CvsUser
import models.daos.logininfo.LoginInfoDao

import scala.collection.mutable
import scala.concurrent.Future

/**
  * Lets us retrieve and save `CvsUser`s. Stores the users in RAM.
  */
class InMemoryUserDao(val loginInfoDao: LoginInfoDao) extends CvsUserDao {

  /**
    * The list of users.
    */
  val users: mutable.HashMap[UUID, CvsUser] = mutable.HashMap()
  val loginInfosToUserIds: mutable.HashMap[LoginInfo, UUID] = mutable.HashMap()

  def save(user: CvsUser, loginInfo: LoginInfo) = {
    users += user.ID -> user
    loginInfosToUserIds += loginInfo -> user.ID
  }

  /**
    * Finds a user by its login info.
    *
    * @param loginInfo the login info of the user to find
    * @return the found user or None if no user for the given login info could be found
    */
  def retrieve(loginInfo: LoginInfo): Future[Option[CvsUser]] =
    Future.successful(loginInfosToUserIds.get(loginInfo).flatMap(userID => users.get(userID)))

  /**
    * Finds a user by their user ID
    *
    * @param userID The ID of the user to find.
    * @return The found user or None if no user for the given ID could be found.
    */
  def retrieve(userID: UUID) = Future.successful(users.get(userID))

  /**
    * Saves a user
    *
    * @param user The user to save.
    */
  def save(user: CvsUser) = {
    users += (user.ID -> user)
  }
}

