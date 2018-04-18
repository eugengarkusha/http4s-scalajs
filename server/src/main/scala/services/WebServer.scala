package services

import java.util.concurrent.ScheduledThreadPoolExecutor

import auth.dto.User
import cats.data.OptionT
import cats.effect.IO
import fs2._
import org.http4s._
import org.http4s.server.blaze.BlazeBuilder
import cats.syntax.semigroupk._
import tsec.authentication.JWTAuthenticator
import tsec.mac.jca.HMACSHA256
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

object WebServer extends App {

  val jwtStatelessAuth: JWTAuthenticator[IO, User, User, HMACSHA256] =
    JWTAuthenticator.unbacked.inBearerToken(
      expiryDuration = 5.seconds, //Absolute expiration time
      maxIdle = None,
      //Basic user data in encoded in subject(no need to pull from DB in services)
      identityStore = OptionT.pure[IO](_),
      signingKey = HMACSHA256.unsafeGenerateKey
    )

  implicit val scheduler = Scheduler.fromScheduledExecutorService(new ScheduledThreadPoolExecutor(2))
  val bootstrapService: HttpService[IO] = new BootStrapService[IO].service
  val authService: HttpService[IO] =
    new AuthService[IO](jwtStatelessAuth, sd => IO.pure(Some(User(1L, "dummyuser")))).service
  val authedService: HttpService[IO] = new AuthedTestService[IO](jwtStatelessAuth).service

  val x = BlazeBuilder[IO]
    .bindHttp(9001, "localhost")
    .mountService(bootstrapService <+> authService <+> authedService, "/")
    .start
    .unsafeToFuture
    .map(s => { println(s.baseUri); s })
  println(s"ServerX is online! localhost:9001")
  Thread.sleep(1099092013)

}
