package models

import java.util.Locale

import slick.driver.PostgresDriver.api._
import scala.concurrent.Future
import play.api.db.DB
import play.api.Play.current

trait DAOComponent {

  def insert(product: CvsProduct): Future[Int]
  def update(id: Long, product: CvsProduct): Future[Int]
  def delete(id: Long): Future[Int]
  def findById(id: Long): Future[CvsProduct]
  def count: Future[Int]
}

object DAO extends DAOComponent {

  private val products = TableQuery[CvsProductTable]

  private def db: Database = Database.forDataSource(DB.getDataSource())

  private def filterQuery(id: Long): Query[CvsProductTable, CvsProduct, Seq] =
    products.filter(_.id === id)

  private def count(filter: String): Future[Int] =
    try db.run(products.filter(_.name.toLowerCase like filter.toLowerCase(Locale.ROOT)).length.result)
    finally db.close()

  override def count: Future[Int] =
    try db.run(products.length.result)
    finally db.close()

  override def findById(id: Long): Future[CvsProduct] =
    try db.run(filterQuery(id).result.head)
    finally db.close()

  override def insert(product: CvsProduct): Future[Int] =
    try db.run(products += product)
    finally db.close()

  override def update(id: Long, update: CvsProduct): Future[Int] =
    try db.run(filterQuery(id).update(update))
    finally db.close()

  override def delete(id: Long): Future[Int] =
    try db.run(filterQuery(id).delete)
    finally db.close()

  def getAll ={
    try db.run(products.result)
    finally db.close()
  }
}

