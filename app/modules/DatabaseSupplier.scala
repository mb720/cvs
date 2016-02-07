package modules

import models.tables._
import play.api.db.DB
import slick.driver.PostgresDriver.api._


/**
  * Supplies the database for this application and creates its tables.
  * Created by Matthias Braun on 2/7/2016.
  */
trait DatabaseSupplier {

  def createTables(db: Database) = {
    val loginInfoTable = TableQuery[LoginInfoTable]
    val users = TableQuery[CvsUserTable]
    val usersWithLoginInfoTable = TableQuery[UsersWithLoginInfoTable]
    val loginInfoToPasswordInfoTable = TableQuery[LoginInfoToPasswordInfoTable]
    val passwordInfoTable = TableQuery[PasswordInfoTable]

    val schemaCreation = DBIO.seq((usersWithLoginInfoTable.schema ++
      loginInfoToPasswordInfoTable.schema ++
      loginInfoTable.schema ++
      passwordInfoTable.schema ++
      users.schema)
      .create)

    db.run(schemaCreation)
    db
  }

  // Has to be lazy, otherwise there is no currently running application
  lazy val db: Database = {
    // Get a database as configured in application.conf
    import play.api.Play.current
    val db = Database.forDataSource(DB.getDataSource())
    createTables(db)
  }
}
