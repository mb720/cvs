package models.daos.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.CvsUser
import models.daos.logininfo.LoginInfoDao
import models.tables.{CvsUserTable, LoginInfoTable, UserWithLoginInfo, UsersWithLoginInfoTable}
import play.api.Logger
import play.api.db.DB
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


/**
  * Created by Matthias Braun on 1/12/2016.
  */
class SqlUserDao(val loginInfoDao: LoginInfoDao, val db: Database) extends CvsUserDao {

  private def loginInfoTable = TableQuery[LoginInfoTable]

  private def users = TableQuery[CvsUserTable]

  private def usersWithLoginInfoTable = TableQuery[UsersWithLoginInfoTable]

//  private val getDatabase = {
//    // Get a database as configured in application.conf
//    import play.api.Play.current
//    val db = Database.forDataSource(DB.getDataSource())
//
//    def createTables() = {
//      // We have to create the users with login info table first
//      val schemaCreation = DBIO.seq((usersWithLoginInfoTable.schema ++ loginInfoTable.schema ++ users.schema).create)
//      db.run(schemaCreation)
//      db
//    }
//    createTables()
//  }
//
//  private def db = getDatabase

  private def filterQuery(id: UUID): Query[CvsUserTable, CvsUser, Seq] = users.filter(_.id === id)

  def count: Future[Int] = db.run(users.length.result)

  def insert(newUser: CvsUser): Future[Int] = db.run(users += newUser)

  def insert(newLoginInfo: LoginInfo): Future[Int] = db.run(loginInfoTable += newLoginInfo)

  def insert(newUserWithLoginInfo: UserWithLoginInfo): Future[Int] = db.run(usersWithLoginInfoTable += newUserWithLoginInfo)

  def update(id: UUID, userToUpdate: CvsUser): Future[Int] = db.run(filterQuery(id).update(userToUpdate))

  def delete(id: UUID): Future[Int] = db.run(filterQuery(id).delete)

  def getAll = db.run(users.result)

  override def retrieve(loginInfo: LoginInfo): Future[Option[CvsUser]] = {
    // From the login info's provider key ("admin@nowhere.com" for example) get the connection from login info to user
    val getUsersWithLoginInfosWithMatchingProviderKey = usersWithLoginInfoTable.filter(_.loginInfoProviderKey === loginInfo.providerKey).result
    db.run(getUsersWithLoginInfosWithMatchingProviderKey).flatMap {
      usersWithLoginInfos =>
        if (usersWithLoginInfos.size > 1) Logger.warn(s"Multiple users for login info $loginInfo")
        usersWithLoginInfos.headOption match {
          case Some(userWithLoginInfo) => retrieve(userWithLoginInfo.userID)
          case None => Logger.warn(s"No user with this login info: $loginInfo"); Future.successful(None)
        }
    }
  }

  /**
    * Finds a user by their user ID.
    *
    * @param userID the ID of the user to find.
    * @return the found user or None if there is no user with that ID
    */
  override def retrieve(userID: UUID): Future[Option[CvsUser]] = {
    Logger.info(s"Finding user in database via ID $userID")
    val getUserByID = users.filter(_.id === userID).result.headOption
    db.run(getUserByID)
  }

  /**
    * Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  def save(user: CvsUser) = {
    Logger.info("Saving user")
    val dbResult = insert(user)
    dbResult.onComplete {
      case Success(nr) => Logger.info(s"User saved: $nr")
      case Failure(ex) => Logger.info(s"User not saved: $ex")
    }
  }

  def save(userWithLoginInfo: UserWithLoginInfo) = {
    Logger.info("Associating user with login info")
    val dbResult = insert(userWithLoginInfo)
    dbResult.onComplete {
      case Success(nr) => Logger.info(s"Associated user with login info: $nr")
      case Failure(ex) => Logger.info(s"Could not associate user with login info: $ex")
    }
  }

  override def save(user: CvsUser, loginInfo: LoginInfo) = {
    save(user)
    loginInfoDao.save(loginInfo)
    save(UserWithLoginInfo(UUID.randomUUID(), loginInfo.providerKey, user.ID))
  }
}
