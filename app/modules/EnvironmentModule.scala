package modules

import com.mohiva.play.silhouette.api.{Environment, EventBus}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.util.{BCryptPasswordHasher, SecureRandomIDGenerator}
import models.CvsUser
import models.daos.PasswordInfoDAO

import play.api.libs.concurrent.Execution.Implicits._

/**
  * Provides the Silhouette environment that includes the module that deals with user authentication and user management (i.e., add, remove, find).
  * Controllers extending this trait can make sure their actions are only called by authorized users.
  */
trait EnvironmentModule extends AuthenticatorServiceModule with UserServiceModule with AuthInfoServiceModule with CredentialsProviderModule {

  lazy val eventBus = EventBus()
  lazy val idGenerator = new SecureRandomIDGenerator
  lazy val authInfoDAO = new PasswordInfoDAO
  lazy val passwordHasher = new BCryptPasswordHasher

  lazy val env = Environment[CvsUser, CookieAuthenticator](
    userService,
    authenticatorService,
    // We have no request providers
    Nil,
    eventBus)
}
