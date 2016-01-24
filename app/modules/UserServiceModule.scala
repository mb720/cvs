package modules

import models.daos.{SqlUserDao, InMemoryUserDao}
import models.services.CvsUserServiceWithDao

/**
  * Lets us add, remove, and search users.
  * Created by Matthias Braun on 1/3/2016.
  */
trait UserServiceModule {

  lazy val userDAO = new SqlUserDao
  lazy val userService = new CvsUserServiceWithDao(userDAO)
}
