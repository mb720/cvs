package models

import java.util.UUID

/**
  * Created by Matthias Braun on 1/15/2016.
  *
  * @param loginInfoProviderKey the provider key of the user's [[com.mohiva.play.silhouette.api.LoginInfo]]
  * @param userID      the ID of the  [[models.CvsUser]]
  */
case class UserWithLoginInfo(id: UUID, loginInfoProviderKey: String, userID: UUID)

