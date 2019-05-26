package services

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ScheduledThreadPoolExecutor

import auth.dto.UserInfo
import fs2._
import org.http4s._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.blaze._
import org.http4s.implicits._
import dal.{SignUpDal, SignUpRecord, UserDal}
import misc.SharedVariables.cookieName
import tsec.authentication.{AuthenticatedCookie, BackingStore, IdentityStore, SecuredRequestHandler, SignedCookieAuthenticator, TSecAuthService, TSecCookieSettings}
import cats.data.{Kleisli, OptionT}
import scalaz.zio._
import tsec.mac.jca.HMACSHA256
import fs2.Stream
import cats.implicits._
import org.http4s.server.Router
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object WebServer extends CatsApp {

  private val key = HMACSHA256.unsafeGenerateKey

  //TODO: Inject
  val usrDal = new UserDal[Task]()
  val signUpDal = new SignUpDal[Task]()

  //Cleaning up expired sign up requests
  //TODO: make expiration time configurable
  val signUpExpirationWorker: Stream[Task, Unit] = {

    def logRemovedRecords(removed: List[SignUpRecord]) = Task {
      val msg = removed.toNel.fold(s"no expierd sign-up records to remove")(nel =>
        s"sucessfully removed expierd sign-up records: ${nel.map(_.email).toList.mkString(",")}")

      println(s"[${Instant.now}] $msg")
    }

    Stream
      .fixedRate[Task](1.minute)
      .>>(Stream.eval(signUpDal.deleteOlderThan(1.minute)))
      .flatMap(v => Stream.eval(logRemovedRecords(v)))
      .handleErrorWith { t =>
        println(s"failed to remove expired sign-up records: $t")
        signUpExpirationWorker
      }
  }


/////// Setting up the authenticator////////////////////////////////////////////////////////////////////////////////

  // Not using token auth for authentication of SPA
  // Reason: http://cryto.net/~joepie91/blog/2016/06/13/stop-using-jwt-for-sessions/
  val cookieAuth: SignedCookieAuthenticator[Task, UserInfo, UserInfo, HMACSHA256] = {

    val settings: TSecCookieSettings = TSecCookieSettings(
      cookieName = cookieName,
      //https://en.wikipedia.org/wiki/Secure_cookies
      secure = false,
      expiryDuration = 20.seconds, // Absolute expiration time
      maxIdle = None, // Rolling window expiration. Makes expiration time refresh after each sucessfull request
      path = Some("/")
    )

    val cookieBackingStore = new BackingStore[Task, UUID, AuthenticatedCookie[HMACSHA256, UserInfo]] {
      // Use DB!
      private val cache: TrieMap[UUID, AuthenticatedCookie[HMACSHA256, UserInfo]] = TrieMap.empty

      override def put(v: AuthenticatedCookie[HMACSHA256, UserInfo]): Task[AuthenticatedCookie[HMACSHA256, UserInfo]] =
        Task(
          cache
            .putIfAbsent(v.id, v)
            .fold(v)(old =>
              throw new AssertionError(s"Trying to Put new cookie $old. Cookie with the same id already exists $v")))

      override def update(v: AuthenticatedCookie[HMACSHA256, UserInfo]): Task[AuthenticatedCookie[HMACSHA256, UserInfo]] =
        Task { cache.update(v.id, v); v }

      override def delete(id: UUID): Task[Unit] = Task(cache.remove(id))

      override def get(id: UUID): OptionT[Task, AuthenticatedCookie[HMACSHA256, UserInfo]] = OptionT(Task(cache.get(id)))
    }

    //use db to check that user exishs and/or if more user info is needed
    val userStore: IdentityStore[Task, UserInfo, UserInfo] = OptionT.pure(_)

    SignedCookieAuthenticator(
      settings,
      cookieBackingStore,
      userStore,
      key
    )
  }
  // provides number of functions: Authenticated => HttpService
  val Auth: SecuredRequestHandler[Task, UserInfo, UserInfo, AuthenticatedCookie[HMACSHA256, UserInfo]] =
    SecuredRequestHandler(cookieAuth)
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////Instantiating services//////////////////////////////////////////////////////////
  // TODO: configure blockingEc properly
  val blockingEc = ExecutionContext.fromExecutor(new ScheduledThreadPoolExecutor(5))

  val serverPort = 9001

  def sendActivationMail(email: String, activationPath: Uri): Task[Unit] =
    Task(println(s"sending activation uri: ${Uri(path = s"localhost:$serverPort/").resolve(activationPath)} to $email"))

  val authServices: AuthServices[AuthenticatedCookie[HMACSHA256, UserInfo]] =
    new AuthServices[AuthenticatedCookie[HMACSHA256, UserInfo]](
      cookieAuth, //ashes to ashes cookie.toCookie
      (cookie, resp) => resp.addCookie(cookie.toCookie),
      usrDal,
      signUpDal,
      sendActivationMail
    )

  val testServices = new TestServices[AuthenticatedCookie[HMACSHA256, UserInfo]]


  val allServices: HttpRoutes[Task] =
    BootStrapService(blockingEc) <+>
      authServices.signInUpService <+>
    // lifting authed services without fallthrough so that if user is not authorized
    // all requests to the unknown endpoints result in 401(Unauthorized).
    // this is done from web crawlers(spidering).
    // IMPORTANT: Authed services must be in the end of the chain otherwise rhe request will shortcut with 401.
      Auth.liftService(
        testServices.authedTestService <+>
        authServices.signOutService
      )

  // TODO: fail with not authorized (see noSpider)
  val httpApp = Router("/" -> allServices).orNotFound

  val server: Task[Nothing] = BlazeServerBuilder[Task]
    .bindHttp(serverPort, "localhost")
    .withHttpApp(httpApp)
    .resource.use(s =>
     Task(println(println(s"ServerX is online! ${s.baseUri}"))) >>
      Task.never
  )

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////Start server and signUpExpirationWorker///////////////////////////////////////////////////////

  override def run(args: List[String]):  ZIO[Environment, Nothing, Int] =
    (signUpExpirationWorker.compile.last -> server).parMapN((_, _: Nothing) => 0).catchAll(t => UIO(t.printStackTrace()).as(1))

}
