
/**
  * Created by Matthias Braun on 21.12.2015.
  */
package modules

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.repositories.DelegableAuthInfoRepository
import com.mohiva.play.silhouette.impl.util._
import models.CvsUser
import models.daos._
import models.services.{UserService, UserServiceImpl}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WSClient

/**
  * This class wires all Silhouette dependencies together. Add this class to `application.conf` via `play.modules.enabled+=thisClass`
  */
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
    * Configures the module
    */
  def configure() {
    bind[UserService].to[UserServiceImpl]
    bind[UserDAO].to[UserDAOImpl]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAO]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  /**
    * Provides the HTTP layer implementation
    *
    * @param client Play's WS client
    * @return the HTTP layer implementation
    */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
    * Provides the Silhouette environment
    *
    * @param userService the user service implementation
    * @param authenticatorService the authentication service implementation
    * @param eventBus the event bus instance
    * @return the Silhouette environment
    */
  @Provides
  def provideEnvironment(
                          userService: UserService,
                          authenticatorService: AuthenticatorService[CookieAuthenticator],
                          eventBus: EventBus): Environment[CvsUser, CookieAuthenticator] = {

    Environment[CvsUser, CookieAuthenticator](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
    * Provides the authenticator service
    *
    * @param fingerprintGenerator the fingerprint generator implementation
    * @param idGenerator the ID generator implementation
    * @param configuration the Play configuration
    * @param clock the clock instance
    * @return the authenticator service
    */
  @Provides
  def provideAuthenticatorService(
                                   fingerprintGenerator: FingerprintGenerator,
                                   idGenerator: IDGenerator,
                                   configuration: Configuration,
                                   clock: Clock): AuthenticatorService[CookieAuthenticator] = {

    // The configuration for this is in conf/silhouette.conf
    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    new CookieAuthenticatorService(config, dao = None, fingerprintGenerator, idGenerator, clock)
  }

  /**
    * Provides the auth info repository.
    *
    * @param passwordInfoDAO the implementation of the delegable password auth info DAO
    * @return the auth info repository instance
    */
  @Provides
  def provideAuthInfoRepository(passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo]): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoDAO)
  }

  /**
    * Provides the credentials provider.
    *
    * @param authInfoRepository the auth info repository implementation
    * @param passwordHasher the default password hasher implementation
    * @return the credentials provider
    */
  @Provides
  def provideCredentialsProvider(
                                  authInfoRepository: AuthInfoRepository,
                                  passwordHasher: PasswordHasher): CredentialsProvider = {
    new CredentialsProvider(authInfoRepository, passwordHasher, Seq(passwordHasher))
  }
}

