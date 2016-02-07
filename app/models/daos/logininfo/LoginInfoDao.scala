package models.daos.logininfo

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo

/**
  * Can save a user's LoginInfo consisting of provider ID (such as credentials) and username.
  * Created by Matthias Braun on 1/24/2016.
  */
trait LoginInfoDao {

  def save(loginInfo: LoginInfo): UUID
}
