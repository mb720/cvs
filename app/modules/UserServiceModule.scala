package modules

import models.daos.UserDAOImpl
import models.services.UserServiceImpl

/**
  * Lets us add, remove, and search users.
  * Created by Matthias Braun on 1/3/2016.
  */
trait UserServiceModule {

  lazy val userDAO = new UserDAOImpl
  lazy val userService = new UserServiceImpl(userDAO)
}
