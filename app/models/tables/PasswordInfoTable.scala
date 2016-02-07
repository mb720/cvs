package models.tables

import com.mohiva.play.silhouette.api.util.PasswordInfo
import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

/**
  * Created by Matthias Braun on 2/7/2016.
  */
//case class PasswordInfoWithId(passwordInfo: PasswordInfo, id: UUID)

class PasswordInfoTable(tag: Tag) extends Table[PasswordInfo](tag, "PASSWORD_INFO") {

  // "bcrypt" for example
  def hasherID = column[String]("HASHER_ID")

  def hashedPassword = column[String]("HASHED_PASSWORD", O.PrimaryKey)

  // The salt can be null in the database
  def salt = column[Option[String]]("SALT")

  def * = (hasherID, hashedPassword, salt) <>(PasswordInfo.tupled, PasswordInfo.unapply)
}
