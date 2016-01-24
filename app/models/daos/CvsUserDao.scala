package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.CvsUser

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait CvsUserDao {
  def save(user: CvsUser, loginInfo: LoginInfo)

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[CvsUser]]

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def retrieve(userID: UUID): Future[Option[CvsUser]]

}
