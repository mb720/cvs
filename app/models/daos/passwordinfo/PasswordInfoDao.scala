package models.daos.passwordinfo

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO

/**
  * Created by Matthias Braun on 1/24/2016.
  */
trait PasswordInfoDao extends DelegableAuthInfoDAO[PasswordInfo] {

}
