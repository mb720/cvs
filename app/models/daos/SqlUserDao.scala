package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.{CvsUser, UserWithLoginInfo}
import play.api.Logger
import play.api.db.DB
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._
import slick.lifted.{TableQuery, Tag}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class LoginInfos(tag: Tag) extends Table[LoginInfo](tag, "LOGIN_INFOS") {

  // The primary key of this table is compound: it consists of the provider's ID and its key
  //def loginInfoID = primaryKey("LOGIN_INFO_ID", (providerID, providerKey))
  //def loginInfoID = column[UUID]("ID", O.PrimaryKey)

  // "credentials" for example
  def providerID = column[String]("PROVIDER_ID")

  // "admin@nowhere.com" for example
  def providerKey = column[String]("PROVIDER_KEY", O.PrimaryKey)

  // A projection telling us how to create a login info from a table row and vice versa. The <> operator creates a bi-directional mapping
  def * = (providerID, providerKey) <>(LoginInfo.tupled, LoginInfo.unapply)
}

class Users(tag: Tag) extends Table[CvsUser](tag, "CVS_USERS") {

  def id = column[UUID]("ID", O.PrimaryKey)

  def firstName = column[String]("FIRST_NAME")

  def lastName = column[String]("LAST_NAME")

  def email = column[String]("EMAIL")

  //def loginInfoProviderKey = column[String]("LOGIN_INFO_PROVIDER_KEY")

  //def loginInfo = foreignKey("LOGIN_INFO_FK", loginInfoProviderKey, TableQuery[LoginInfos])(_.providerKey)

  // A projection telling us how to create a user from a table row and vice versa. The <> operator creates a bi-directional mapping
  def * = (id, firstName, lastName, email) <>(CvsUser.tupled, CvsUser.unapply)
}

class UsersWithLoginInfos(tag: Tag) extends Table[UserWithLoginInfo](tag, "CVS_USERS_WITH_LOGIN_INFOS") {
  // A reified foreign key relation that can be navigated to create a join
  //def loginInfo = foreignKey("LOGIN_INFO_FK", loginInfoID, TableQuery[LoginInfos])(_.loginInfoID)

  //def relationID = primaryKey("USER_WITH_LOGIN_INFO_ID", (loginInfoProviderKey, userID))
  def relationID = column[UUID]("USER_WITH_LOGIN_INFO_ID", O.PrimaryKey)

  def loginInfoProviderKey = column[String]("LOGIN_INFO_PROVIDER_KEY")

  def userID = column[UUID]("USER_ID")

  def * = (relationID, loginInfoProviderKey, userID) <>(UserWithLoginInfo.tupled, UserWithLoginInfo.unapply)
}

/**
  * Created by Matthias Braun on 1/12/2016.
  */
class SqlUserDao() extends CvsUserDao {

  private val loginInfos = TableQuery[LoginInfos]
  private val users = TableQuery[Users]
  private val usersWithLoginInfos = TableQuery[UsersWithLoginInfos]

  private def getDatabase = {
    // Get a database as configured in application.conf
    import play.api.Play.current
    val db = Database.forDataSource(DB.getDataSource())

    def createTables(db: PostgresDriver.backend.DatabaseDef) = {
      // Create the tables
      // We have to create the users with login info table first
      val schemaCreation = DBIO.seq((usersWithLoginInfos.schema ++ loginInfos.schema ++ users.schema).create)
      db.run(schemaCreation)
      db
    }
    createTables(db)
  }

  private def db = getDatabase

  private def filterQuery(id: UUID): Query[Users, CvsUser, Seq] = users.filter(_.id === id)

  def count: Future[Int] = db.run(users.length.result)

  //def findById(id: Long): Future[CvsUser] = db.run(filterQuery(id).result.head)

  def insert(newUser: CvsUser): Future[Int] = db.run(users += newUser)

  def insert(newLoginInfo: LoginInfo): Future[Int] = db.run(loginInfos += newLoginInfo)

  def insert(newUserWithLoginInfo: UserWithLoginInfo): Future[Int] = db.run(usersWithLoginInfos += newUserWithLoginInfo)

  def update(id: UUID, update: CvsUser): Future[Int] = db.run(filterQuery(id).update(update))

  def delete(id: UUID): Future[Int] = db.run(filterQuery(id).delete)

  def getAll = db.run(users.result)


  override def retrieve(loginInfo: LoginInfo): Future[Option[CvsUser]] = {
    // From the login info's provider key ("admin@nowhere.com" for example) get the connection from login info to user
    val getUsersWithLoginInfosWithMatchingProviderKey = usersWithLoginInfos.filter(_.loginInfoProviderKey === loginInfo.providerKey).result
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

  def save(loginInfo: LoginInfo): UUID = {
    Logger.info("Saving login info")
    val loginInfoID = UUID.randomUUID

    val dbResult = insert(loginInfo)
    dbResult.onComplete {
      case Success(nr) => Logger.info(s"Login info saved: $nr ")
      case Failure(ex) => Logger.info(s"Login info not saved: $ex ")
    }
    loginInfoID
  }

  def save(userWithLoginInfo: UserWithLoginInfo) = {
    Logger.info("Associating user with login info")
    val dbResult = insert(userWithLoginInfo)
    dbResult.onComplete {
      case Success(nr) => Logger.info(s"Associated user with login info: $nr ")
      case Failure(ex) => Logger.info(s"Could not associate user with login info: $ex ")
    }
  }

  override def save(user: CvsUser, loginInfo: LoginInfo) = {
    save(user)
    save(loginInfo)
    save(UserWithLoginInfo(UUID.randomUUID(), loginInfo.providerKey, user.ID))
  }
}
