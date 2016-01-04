package modules

import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{Clock, IDGenerator}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.util.DefaultFingerprintGenerator

import scala.concurrent.duration._

/**
  * Provides the way users are authenticated when they perform a request.
  */
trait AuthenticatorServiceModule {

  lazy val cookieSettings = CookieAuthenticatorSettings(
    cookieName = "authenticator",
    cookiePath = "/",
    // Requires SSL
    secureCookie = true,
    // Cookie is not accessible from client-side JavaScript code
    httpOnlyCookie = true,
    useFingerprinting = true,
    // We use a transient cookie that is not stored on the user's disk but in memory and is thus deleted when they close the browser
    cookieMaxAge = None,
    authenticatorIdleTimeout = Some(30.minutes),
    authenticatorExpiry = 12.hours
  )

  lazy val authenticatorService: AuthenticatorService[CookieAuthenticator] = {

    // The CookieAuthenticatorService needs an ExecutionContext
    import play.api.libs.concurrent.Execution.Implicits._
    val fingerprintGenerator = new DefaultFingerprintGenerator(includeRemoteAddress = false)
    new CookieAuthenticatorService(cookieSettings, dao = None, fingerprintGenerator, idGenerator, Clock())
  }

  def idGenerator: IDGenerator

}
