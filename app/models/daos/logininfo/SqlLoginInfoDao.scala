package models.daos.logininfo

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.tables.LoginInfoTable
import play.api.Logger
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by Matthias Braun on 1/24/2016.
  */
class SqlLoginInfoDao(db: Database) extends LoginInfoDao {

  private val loginInfoTable = TableQuery[LoginInfoTable]

  //  private val getDatabase = {
  //    // Get a database as configured in application.conf
  //    import play.api.Play.current
  //    val db = Database.forDataSource(DB.getDataSource())
  //
  //    def createTables() = {
  //      // We have to create the users with login info table first
  //      val schemaCreation = DBIO.seq(loginInfoTable.schema.create)
  //      db.run(schemaCreation)
  //      db
  //    }
  //
  //    createTables()
  //  }
  //
  //  private def db = getDatabase

  def insert(newLoginInfo: LoginInfo): Future[Int] = db.run(loginInfoTable.insertOrUpdate(newLoginInfo))

  override def save(loginInfo: LoginInfo): UUID = {
    Logger.info("Saving login info")
    val loginInfoID = UUID.randomUUID

    val dbResult = insert(loginInfo)
    dbResult.onComplete {
      case Success(nr) => Logger.info(s"Login info saved: $nr ")
      case Failure(ex) => Logger.info(s"Login info not saved: $ex ")
    }
    loginInfoID
  }
}
