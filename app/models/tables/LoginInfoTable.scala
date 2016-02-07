package models.tables

import com.mohiva.play.silhouette.api.LoginInfo
import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

/**
  * Created by Matthias Braun on 2/7/2016.
  */

class LoginInfoTable(tag: Tag) extends Table[LoginInfo](tag, "LOGIN_INFOS") {

  // "credentials" for example
  def providerID = column[String]("PROVIDER_ID")

  // "admin@nowhere.com" for example
  def providerKey = column[String]("PROVIDER_KEY", O.PrimaryKey)

  // A projection telling us how to create a login info from a table row and vice versa. The <> operator creates a bi-directional mapping
  def * = (providerID, providerKey) <>(LoginInfo.tupled, LoginInfo.unapply)
}
