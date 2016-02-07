package models.tables

import java.util.UUID

import models.CvsUser
import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

/**
  * Created by Matthias Braun on 2/7/2016.
  */
class CvsUserTable(tag: Tag) extends Table[CvsUser](tag, "CVS_USERS") {

  def id = column[UUID]("ID", O.PrimaryKey)

  def name = column[String]("NAME")

  def email = column[String]("EMAIL")

  // A projection telling us how to create a user from a table row and vice versa. The <> operator creates a bi-directional mapping
  def * = (id, name, email) <>(CvsUser.tupled, CvsUser.unapply)
}
