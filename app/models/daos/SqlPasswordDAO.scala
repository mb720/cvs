package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO

import scala.concurrent.Future

/**
  * Created by Matthias Braun on 1/24/2016.
  */
class SqlPasswordDao extends DelegableAuthInfoDAO[PasswordInfo]{
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = ???

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def remove(loginInfo: LoginInfo): Future[Unit] = ???

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???
}
