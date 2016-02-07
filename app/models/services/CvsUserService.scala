package models.services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.CvsUser

/**
  * Handles actions to users.
  */
trait CvsUserService extends IdentityService[CvsUser] {

  /**
    * Saves a user.
    *
    * @param user      the user to save
    * @param loginInfo the user's login info consisting of the way they authenticated (e.g., via credentials) and the their unique identifier (e.g., their email address)
    */
  def save(user: CvsUser, loginInfo: LoginInfo)
}
