package models.tables

import java.util.UUID

import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

/**
  * Created by Matthias Braun on 1/15/2016.
  *
  * @param loginInfoProviderKey the provider key of the user's [[com.mohiva.play.silhouette.api.LoginInfo]]
  * @param userID               the ID of the  [[models.CvsUser]]
  */
case class UserWithLoginInfo(id: UUID, loginInfoProviderKey: String, userID: UUID)

class UsersWithLoginInfoTable(tag: Tag) extends Table[UserWithLoginInfo](tag, "CVS_USERS_WITH_LOGIN_INFO") {

  def relationID = column[UUID]("USER_WITH_LOGIN_INFO_ID", O.PrimaryKey)

  def loginInfoProviderKey = column[String]("LOGIN_INFO_PROVIDER_KEY")

  def userID = column[UUID]("USER_ID")

  def * = (relationID, loginInfoProviderKey, userID) <>(UserWithLoginInfo.tupled, UserWithLoginInfo.unapply)
}
