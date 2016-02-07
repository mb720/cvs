package models.daos.passwordinfo

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import models.daos.logininfo.LoginInfoDao
import models.tables.{LoginInfoToPasswordInfo, LoginInfoToPasswordInfoTable, PasswordInfoTable}
import play.api.Logger
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Matthias Braun on 1/24/2016.
  */
class SqlPasswordDao(loginInfoDao: LoginInfoDao, db: Database) extends PasswordInfoDao {

  private val loginInfoToPasswordInfoTable = TableQuery[LoginInfoToPasswordInfoTable]

  private val passwordInfoTable = TableQuery[PasswordInfoTable]

  //  private val getDatabase = {
  //    // Get a database as configured in application.conf
  //    import play.api.Play.current
  //    val db = Database.forDataSource(DB.getDataSource())
  //
  //    def createTables() = {
  //      val schemaCreation = DBIO.seq((loginInfoToPasswordInfoTable.schema ++ passwordInfoTable.schema).create)
  //      Logger.info("Creating tables: loginInfoToPasswordInfoTable and passwordInfoTable")
  //      db.run(schemaCreation)
  //      db
  //    }
  //
  //    createTables()
  //  }

  def getPasswordInfo(loginInfoToPasswordInfo: LoginInfoToPasswordInfo): Future[Option[PasswordInfo]] = {
    val getPasswordInfoAction = passwordInfoTable.filter(_.hashedPassword === loginInfoToPasswordInfo.hashedPassword).result
    db.run(getPasswordInfoAction).map {
      passwordInfos =>
        if (passwordInfos.size > 1) Logger.warn(s"Multiple password infos")
        passwordInfos.headOption match {
          //case Some(passwordInfoWithoutSalt) => Some(PasswordInfo(passwordInfoWithoutSalt.hasherId, passwordInfoWithoutSalt.hashedPassword))
          case Some(passwordInfo) => Some(passwordInfo)
          case None => Logger.warn(s"No password info with this hash"); None
        }
    }
  }

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {

    val getLoginInfoToPasswordInfo = loginInfoToPasswordInfoTable.filter(_.loginInfoProviderKey === loginInfo.providerKey).result
    db.run(getLoginInfoToPasswordInfo).flatMap {
      loginInfoToPasswordInfos =>
        loginInfoToPasswordInfos.headOption match {
          case Some(loginInfoToPasswordInfo) => getPasswordInfo(loginInfoToPasswordInfo)
          case None => Logger.warn(s"No password info with this login info: $loginInfo"); Future.successful(None)
        }
    }
  }

  private def getLoginInfoToPasswordInfo(loginInfo: LoginInfo) = loginInfoToPasswordInfoTable.filter(_.loginInfoProviderKey === loginInfo.providerKey)

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val updateAction = getLoginInfoToPasswordInfo(loginInfo).update(LoginInfoToPasswordInfo(loginInfo.providerKey, authInfo.password))
    db.run(updateAction).map { result => Logger.info(s"Update result: $result"); authInfo }
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = Future(Logger.info("Removing login info not yet implemented"))

  override def save(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = {
    val saveAction = DBIO.seq(passwordInfoTable += passwordInfo, associate(loginInfo, passwordInfo))

    db.run(saveAction).map { result => Logger.info(s"Result of adding password info and login info: $result"); passwordInfo }
      .recover { case exc => Logger.warn(s"Exception: $exc"); passwordInfo }
  }

  def associate(loginInfo: LoginInfo, passwordInfo: PasswordInfo) =
    loginInfoToPasswordInfoTable += LoginInfoToPasswordInfo(loginInfo.providerKey, passwordInfo.password)

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = save(loginInfo, authInfo)
}
