package models

import slick.lifted.Tag
import slick.driver.PostgresDriver.api._

class CvsProductTable(tag: Tag) extends Table[CvsProduct](tag, "cvs_products") {

  def id = column[Long]("id", O.PrimaryKey)
  def name = column[String]("name")

  def * = (id, name) <> (CvsProduct.tupled, CvsProduct.unapply )
}
