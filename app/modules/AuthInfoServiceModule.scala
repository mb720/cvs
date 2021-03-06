package modules

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.repositories.DelegableAuthInfoRepository
import models.daos.passwordinfo.PasswordInfoDao
import play.api.libs.concurrent.Execution.Implicits._

/**
  * Provides a way of saving authorization info about users such as their email and their hashed password
  * Created by Matthias Braun on 1/3/2016.
  */
trait AuthInfoServiceModule {

  def passwordInfoDao: PasswordInfoDao

  lazy val authInfoRepo = new DelegableAuthInfoRepository(passwordInfoDao)

}
