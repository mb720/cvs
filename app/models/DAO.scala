package models

import slick.driver.PostgresDriver.api._
import scala.concurrent.Future
import play.api.db.DB
import play.api.Play.current

trait DAOComponent {

  def insert(msg: MyMessage): Future[Int]
  def update(id: Long, msg: MyMessage): Future[Int]
  def delete(id: Long): Future[Int]
  def findById(id: Long): Future[MyMessage]
  def count: Future[Int]
}

object DAO extends DAOComponent {

  private val messages = TableQuery[MyMessages]

  private def db: Database = Database.forDataSource(DB.getDataSource())

  private def filterQuery(id: Long): Query[MyMessages, MyMessage, Seq] =
    messages.filter(_.id === id)

  private def count(filter: String): Future[Int] =
    try db.run(messages.filter(_.content.toLowerCase like filter.toLowerCase()).length.result)
    finally db.close

  override def count: Future[Int] =
    try db.run(messages.length.result)
    finally db.close

  override def findById(id: Long): Future[MyMessage] =
    try db.run(filterQuery(id).result.head)
    finally db.close

  override def insert(msg: MyMessage): Future[Int] =
    try db.run(messages += msg)
    finally db.close

  override def update(id: Long, msg: MyMessage): Future[Int] =
    try db.run(filterQuery(id).update(msg))
    finally db.close

  override def delete(id: Long): Future[Int] =
    try db.run(filterQuery(id).delete)
    finally db.close

  def getAll ={
    try db.run(messages.result)
    finally db.close
  }
}

