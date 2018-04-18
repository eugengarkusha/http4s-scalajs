package services

import java.time.{Instant, ZonedDateTime}

import _root_.io.circe.syntax._
import auth.dto.{SignInData, User}
import cats.data.OptionT
import cats.effect.{Effect, IO}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{InvariantMonoidal, Monad}
import org.http4s.Credentials.Token
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.circe._
import org.http4s.{HttpService, _}
import tsec.authentication._

import scala.concurrent.duration._

class AuthedTestService[F[_], A](authentiator: Authenticator[F, User, User, A])(implicit F: Effect[F],
                                                                                m: InvariantMonoidal[F]) {

  implicit val dsl = Http4sDsl[F]

  import dsl._

  val Auth: SecuredRequestHandler[F, User, User, A] = SecuredRequestHandler(authentiator)

  /*
  Now from here, if want want to create services, we simply use the following
  (Note: Since the type of the service is HttpService[F], we can mount it like any other endpoint!):
   */
  val service: HttpService[F] = Auth.liftService(
    TSecAuthService {
      //Where user is the case class User above
      case request @ GET -> Root / "api" / "test" asAuthed user =>
        /*
      Note: The request is of type: SecuredRequest, which carries:
      1. The request
      2. The Authenticator (i.e token)
      3. The identity (i.e in this case, User)
         */

        Ok.apply(s"tested at ${ZonedDateTime.now()}. User data in cookie: $user".asJson)
    }
  )

}
