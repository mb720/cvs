package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{Environment, LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignInForm
import models.CvsUser
import models.services.UserService
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
  * Created by Matthias Braun on 21.12.2015.
  */
class SignInController @Inject()(val messagesApi: MessagesApi,
                                 val env: Environment[CvsUser, CookieAuthenticator], userService: UserService, authInfoRepo: AuthInfoRepository, passwordHasher: PasswordHasher)
  extends Silhouette[CvsUser, CookieAuthenticator] {

  def createAdmin(adminLoginInfo: LoginInfo): CvsUser = {
    Logger.info("Creating admin")
    CvsUser(
      userID = UUID.randomUUID(),
      loginInfo = adminLoginInfo,
      firstName = Some("Ad"),
      lastName = Some("Min"),
      fullName = Some("Admin"),
      email = Some(adminLoginInfo.providerKey),
      avatarURL = None
    )
  }

  def makeSureThereIsAdmin() = {
    Logger.info("Making sure there is an admin user")
    val adminLoginInfo = LoginInfo(CredentialsProvider.ID, "admin@nowhere.com")
    userService.retrieve(adminLoginInfo).flatMap {
      case Some(admin) => Future.successful(Logger.info("Admin already exists"))
      case None => Future.successful {
        userService.save(createAdmin(adminLoginInfo))
        val passHash = passwordHasher.hash("ezgportal")
        authInfoRepo.add(adminLoginInfo, passHash)
      }
    }
  }

  /**
    * Shows the sign in form
    */
  def signIn = UserAwareAction.async { implicit request =>
    Logger.info("Show sign in page")
    makeSureThereIsAdmin()
    request.identity match {
      case Some(user) => Future.successful(Ok(s"You are already signed in, ${user.firstName.getOrElse("user")}"))
      case None => Future.successful {
        Logger.info("There's nobody logged in yet")
        Ok(views.html.signIn(SignInForm.form))
      }
    }
  }
}
