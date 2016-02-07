package modules

import models.daos.logininfo.SqlLoginInfoDao
import models.daos.user.SqlUserDao
import models.services.CvsUserServiceWithDao

/**
  * Lets us add, remove, and search users.
  * Created by Matthias Braun on 1/3/2016.
  */
trait UserServiceModule {

  this: DatabaseSupplier =>

  lazy val loginInfoDao = new SqlLoginInfoDao(db)
  lazy val userDAO = new SqlUserDao(loginInfoDao, db)
  lazy val userService = new CvsUserServiceWithDao(userDAO)
}
