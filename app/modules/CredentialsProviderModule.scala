package modules

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.libs.concurrent.Execution.Implicits._
/**
  * Lets users log in using password and name.
  * Created by Matthias Braun on 1/3/2016.
  */
trait CredentialsProviderModule {

  lazy val credentialsProvider = new CredentialsProvider(authInfoRepo, passwordHasher, Seq(passwordHasher))

  def authInfoRepo: AuthInfoRepository

  def passwordHasher: PasswordHasher
}
