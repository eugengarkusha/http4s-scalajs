package services

import java.util.UUID
import java.util.concurrent.ScheduledThreadPoolExecutor

import auth.dto.User
import cats.data.{NonEmptyList, OptionT}
import cats.effect.IO
import fs2._
import org.http4s.headers
import org.http4s._
import org.http4s.server.blaze.BlazeBuilder
import cats.syntax.semigroupk._
import org.http4s.headers.Authorization
import tsec.authentication.{
  AugmentedJWT,
  AuthenticatedCookie,
  BackingStore,
  IdentityStore,
  SignedCookieAuthenticator,
  TSecCookieSettings
}
import tsec.mac.jca.HMACSHA256

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object WebServer extends App {

  private val key = HMACSHA256.unsafeGenerateKey
  implicit val scheduler = Scheduler.fromScheduledExecutorService(new ScheduledThreadPoolExecutor(2))


/////// Setting up the authenticator////////////////////////////////////////////////////////////////////////////////

  // Not using token auth for authentication of SPA
  // Reason: http://cryto.net/~joepie91/blog/2016/06/13/stop-using-jwt-for-sessions/

  val settings: TSecCookieSettings = TSecCookieSettings(
    cookieName = "app-auth",
    //https://en.wikipedia.org/wiki/Secure_cookies
    secure = false,
    expiryDuration = 20.seconds, // Absolute expiration time
    maxIdle = None, // Rolling window expiration. Makes expiration time refresh after each sucessfull request
    path = Some("/")

  )

  val cookieBackingStore = new BackingStore[IO, UUID, AuthenticatedCookie[HMACSHA256, User]] {
    // Use DB!
    private val cache: TrieMap[UUID, AuthenticatedCookie[HMACSHA256, User]] = TrieMap.empty

    override def put(v: AuthenticatedCookie[HMACSHA256, User]): IO[AuthenticatedCookie[HMACSHA256, User]] =
      IO(
        cache
          .putIfAbsent(v.id, v)
          .fold(v)(old =>
            throw new AssertionError(s"Trying to Put new cookie $old. Cookie with the same id already exists $v")))

    override def update(v: AuthenticatedCookie[HMACSHA256, User]): IO[AuthenticatedCookie[HMACSHA256, User]] =
      IO { cache.update(v.id, v); v }

    override def delete(id: UUID): IO[Unit] = IO(cache.remove(id))

    override def get(id: UUID): OptionT[IO, AuthenticatedCookie[HMACSHA256, User]] = OptionT(IO(cache.get(id)))
  }

  //use db to check that user exishs and/or if more user info is needed
  val userStore: IdentityStore[IO, User, User] = OptionT.pure(_)

  val cookieAuth: SignedCookieAuthenticator[IO, User, User, HMACSHA256] =
    SignedCookieAuthenticator(
      settings,
      cookieBackingStore,
      userStore,
      key
    )
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////Instantiating services//////////////////////////////////////////////////////////
  val bootstrapService: HttpService[IO] = new BootStrapService[IO].service

  val authService: HttpService[IO] =
    new AuthService[IO, AuthenticatedCookie[HMACSHA256, User]](
      cookieAuth, //ashes to ashes cookie.toCookie
      (cookie, resp) => resp.addCookie(cookie.toCookie),
      sd => IO.pure(Some(User(1L, "dummyuser")))
    ).service

  val authedService: HttpService[IO] =
    new AuthedTestService[IO, AuthenticatedCookie[HMACSHA256, User]](cookieAuth).service

  val allServices: HttpService[IO] = bootstrapService <+> authService <+> authedService
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////Start server////////////////////////////////////////////////////////////////////////
  val server = BlazeBuilder[IO]
    .bindHttp(9001, "localhost")
    .mountService(allServices, "/")
    .start
    .unsafeRunSync()

  println(s"ServerX is online! ${server.baseUri}")
  Thread.sleep(1099092013)

}
