package models.tables
import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

/**
  * Created by Matthias Braun on 11/15/2015.
  */
case class CvsProduct(id: Long, name: String)

class CvsProductTable(tag: Tag) extends Table[CvsProduct](tag, "CVS_PRODUCTS") {
  def id = column[Long]("ID", O.PrimaryKey)

  def name = column[String]("NAME")

  def * = (id, name) <>(CvsProduct.tupled, CvsProduct.unapply)
}
