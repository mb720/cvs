
/**
  * Created by Matthias Braun on 21.12.2015.
  */
package modules

import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{ Environment, EventBus }
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{ CookieSecretProvider, CookieSecretSettings }
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.oauth2._
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{ CookieStateProvider, CookieStateSettings, DummyStateProvider }
import com.mohiva.play.silhouette.impl.providers.openid.YahooProvider
import com.mohiva.play.silhouette.impl.providers.openid.services.PlayOpenIDService
import com.mohiva.play.silhouette.impl.repositories.DelegableAuthInfoRepository
import com.mohiva.play.silhouette.impl.services._
import com.mohiva.play.silhouette.impl.util._
import models.User
import models.daos._
import models.services.{ UserService, UserServiceImpl }
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.openid.OpenIdClient
import play.api.libs.ws.WSClient

/**
  * The Guice module which wires all Silhouette dependencies. Add this class to `application.conf` via `play.modules.enabled+=thisClass`
  */
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
    * Configures the module.
    */
  def configure() {
    bind[UserService].to[UserServiceImpl]
    bind[UserDAO].to[UserDAOImpl]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAO]
    bind[DelegableAuthInfoDAO[OAuth1Info]].to[OAuth1InfoDAO]
    bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDAO]
    bind[DelegableAuthInfoDAO[OpenIDInfo]].to[OpenIDInfoDAO]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  /**
    * Provides the HTTP layer implementation.
    *
    * @param client Play's WS client.
    * @return The HTTP layer implementation.
    */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
    * Provides the Silhouette environment.
    *
    * @param userService The user service implementation.
    * @param authenticatorService The authentication service implementation.
    * @param eventBus The event bus instance.
    * @return The Silhouette environment.
    */
  @Provides
  def provideEnvironment(
                          userService: UserService,
                          authenticatorService: AuthenticatorService[CookieAuthenticator],
                          eventBus: EventBus): Environment[User, CookieAuthenticator] = {

    Environment[User, CookieAuthenticator](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }


  /**
    * Provides the authenticator service.
    *
    * @param fingerprintGenerator The fingerprint generator implementation.
    * @param idGenerator The ID generator implementation.
    * @param configuration The Play configuration.
    * @param clock The clock instance.
    * @return The authenticator service.
    */
  @Provides
  def provideAuthenticatorService(
                                   fingerprintGenerator: FingerprintGenerator,
                                   idGenerator: IDGenerator,
                                   configuration: Configuration,
                                   clock: Clock): AuthenticatorService[CookieAuthenticator] = {

    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    new CookieAuthenticatorService(config, None, fingerprintGenerator, idGenerator, clock)
  }

  /**
    * Provides the auth info repository.
    *
    * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
    * @param oauth1InfoDAO The implementation of the delegable OAuth1 auth info DAO.
    * @param oauth2InfoDAO The implementation of the delegable OAuth2 auth info DAO.
    * @param openIDInfoDAO The implementation of the delegable OpenID auth info DAO.
    * @return The auth info repository instance.
    */
  @Provides
  def provideAuthInfoRepository(
                                 passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
                                 oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
                                 oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info],
                                 openIDInfoDAO: DelegableAuthInfoDAO[OpenIDInfo]): AuthInfoRepository = {

    new DelegableAuthInfoRepository(passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO, openIDInfoDAO)
  }
  /**
    * Provides the credentials provider.
    *
    * @param authInfoRepository The auth info repository implementation.
    * @param passwordHasher The default password hasher implementation.
    * @return The credentials provider.
    */
  @Provides
  def provideCredentialsProvider(
                                  authInfoRepository: AuthInfoRepository,
                                  passwordHasher: PasswordHasher): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasher, Seq(passwordHasher))
  }
}
