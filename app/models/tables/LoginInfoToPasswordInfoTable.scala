package models.tables

import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

/**
  * Created by Matthias Braun on 2/7/2016.
  */
case class LoginInfoToPasswordInfo(loginInfoProviderKey: String, hashedPassword: String)

class LoginInfoToPasswordInfoTable(tag: Tag) extends Table[LoginInfoToPasswordInfo](tag, "LOGIN_INFO_TO_PASSWORD_INFO") {

  def loginInfoProviderKey = column[String]("LOGIN_INFO_PROVIDER_KEY", O.PrimaryKey)

  def hashedPassword = column[String]("HASHED_PASSWORD")

  def * = (loginInfoProviderKey, hashedPassword) <>(LoginInfoToPasswordInfo.tupled, LoginInfoToPasswordInfo.unapply)
}
