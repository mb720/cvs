package models

import java.util.Date
import java.sql.{ Date => SqlDate }
import play.api.Play.current
import slick.lifted.Tag
import java.sql.Timestamp
import slick.driver.PostgresDriver.api._

case class MyMessage(id: Option[Long], content: String)

class MyMessages(tag: Tag) extends Table[MyMessage](tag, "MESSAGE") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def content = column[String]("content")

  def * = (id.?, content) <> (MyMessage.tupled, MyMessage.unapply _)
}
