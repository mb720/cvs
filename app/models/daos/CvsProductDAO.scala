package models.daos

import java.util.Locale

import models.{CvsProductTable, CvsProduct}
import play.api.Play.current
import play.api.db.DB
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

trait DAOComponent {

  def insert(product: CvsProduct): Future[Int]
  def update(id: Long, product: CvsProduct): Future[Int]
  def delete(id: Long): Future[Int]
  def findById(id: Long): Future[CvsProduct]
  def count: Future[Int]
}

object CvsProductDAO extends DAOComponent {

  private val products = TableQuery[CvsProductTable]

  private def db: Database = Database.forDataSource(DB.getDataSource())

  private def filterQuery(id: Long): Query[CvsProductTable, CvsProduct, Seq] =
    products.filter(_.id === id)

  private def count(filter: String): Future[Int] =
    db.run(products.filter(_.name.toLowerCase like filter.toLowerCase(Locale.ROOT)).length.result)

  override def count: Future[Int] =
    db.run(products.length.result)

  override def findById(id: Long): Future[CvsProduct] =
    db.run(filterQuery(id).result.head)

  override def insert(product: CvsProduct): Future[Int] =
    db.run(products += product)

  override def update(id: Long, update: CvsProduct): Future[Int] =
    db.run(filterQuery(id).update(update))

  override def delete(id: Long): Future[Int] =
    db.run(filterQuery(id).delete)

  def getAll ={
    db.run(products.result)
  }
}

