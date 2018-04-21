package services

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ScheduledThreadPoolExecutor

import auth.dto.UserInfo
import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import fs2._
import fs2.Stream.ToEffect
import org.http4s._
import org.http4s.server.blaze.BlazeBuilder
import cats.syntax.semigroupk._
import dal.{SignUpDal, UserDal}
import misc.SharedVariables.cookieName
import tsec.authentication.{
  AuthenticatedCookie,
  BackingStore,
  IdentityStore,
  SecuredRequestHandler,
  SignedCookieAuthenticator,
  TSecAuthService,
  TSecCookieSettings
}
import tsec.mac.jca.HMACSHA256

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object WebServer extends App {

  private val key = HMACSHA256.unsafeGenerateKey
  implicit val scheduler = Scheduler.fromScheduledExecutorService(new ScheduledThreadPoolExecutor(2))

  import cats.syntax.applicativeError._
  //TODO: Inject
  val usrDal = new UserDal[IO]()
  val signUpDal = new SignUpDal[IO]()

  //Cleaning up expired sign up requests
  //TODO: make expiration time configurable
  scheduler
    .fixedRate[IO](1.minute)
    .>>(Stream.eval(signUpDal.deleteOlderThan(1.minute)))
    .compile
    .last
    .unsafeRunAsync(_.fold(err => println(s"failed to clean up sign-up  records: $err"),
                           _ => println(s"sucessfullu cleaned up sign-up  records at ${Instant.now}")))

/////// Setting up the authenticator////////////////////////////////////////////////////////////////////////////////

  // Not using token auth for authentication of SPA
  // Reason: http://cryto.net/~joepie91/blog/2016/06/13/stop-using-jwt-for-sessions/
  val cookieAuth: SignedCookieAuthenticator[IO, UserInfo, UserInfo, HMACSHA256] = {

    val settings: TSecCookieSettings = TSecCookieSettings(
      cookieName = cookieName,
      //https://en.wikipedia.org/wiki/Secure_cookies
      secure = false,
      expiryDuration = 20.seconds, // Absolute expiration time
      maxIdle = None, // Rolling window expiration. Makes expiration time refresh after each sucessfull request
      path = Some("/")
    )

    val cookieBackingStore = new BackingStore[IO, UUID, AuthenticatedCookie[HMACSHA256, UserInfo]] {
      // Use DB!
      private val cache: TrieMap[UUID, AuthenticatedCookie[HMACSHA256, UserInfo]] = TrieMap.empty

      override def put(v: AuthenticatedCookie[HMACSHA256, UserInfo]): IO[AuthenticatedCookie[HMACSHA256, UserInfo]] =
        IO(
          cache
            .putIfAbsent(v.id, v)
            .fold(v)(old =>
              throw new AssertionError(s"Trying to Put new cookie $old. Cookie with the same id already exists $v")))

      override def update(v: AuthenticatedCookie[HMACSHA256, UserInfo]): IO[AuthenticatedCookie[HMACSHA256, UserInfo]] =
        IO { cache.update(v.id, v); v }

      override def delete(id: UUID): IO[Unit] = IO(cache.remove(id))

      override def get(id: UUID): OptionT[IO, AuthenticatedCookie[HMACSHA256, UserInfo]] = OptionT(IO(cache.get(id)))
    }

    //use db to check that user exishs and/or if more user info is needed
    val userStore: IdentityStore[IO, UserInfo, UserInfo] = OptionT.pure(_)

    SignedCookieAuthenticator(
      settings,
      cookieBackingStore,
      userStore,
      key
    )
  }
  // provides number of functions: Authenticated => HttpService
  val Auth: SecuredRequestHandler[IO, UserInfo, UserInfo, AuthenticatedCookie[HMACSHA256, UserInfo]] =
    SecuredRequestHandler(cookieAuth)
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////Instantiating services//////////////////////////////////////////////////////////
  val bootstrapService: HttpService[IO] = new BootStrapService[IO].service

  def sendActivationMail(email: String, activationPath: Uri): IO[Unit] =
    IO(println(s"sending activation uri: ${baseUri.resolve(activationPath)} to $email"))

  val authServices: AuthServices[IO, AuthenticatedCookie[HMACSHA256, UserInfo]] =
    new AuthServices[IO, AuthenticatedCookie[HMACSHA256, UserInfo]](
      cookieAuth, //ashes to ashes cookie.toCookie
      (cookie, resp) => resp.addCookie(cookie.toCookie),
      usrDal,
      signUpDal,
      sendActivationMail
    )

  val testServices =
    new TestServices[IO, AuthenticatedCookie[HMACSHA256, UserInfo]](cookieAuth)

  val allServices: HttpService[IO] =
    bootstrapService <+>
      authServices.signInUpService <+>
      /*
       * Composing authenticated services separately.
       * Reason:  Auth.liftService translates Option(NotFound) to NotAuthrorized(not authenticated)
       * to protect from web crawlers(spidering). The same idea as in org.http4s.AuthMiddleware.noSpider
       */
      Auth.liftService(
        testServices.authedTestService <+>
          authServices.signOutService
      )

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////Start server////////////////////////////////////////////////////////////////////////
  val server = BlazeBuilder[IO]
    .bindHttp(9001, "localhost")
    .mountService(allServices, "/")
    .start
    .unsafeRunSync()

  lazy val baseUri = server.baseUri
  println(s"ServerX is online! ${server.baseUri}")
  Thread.sleep(1099092013)

}
