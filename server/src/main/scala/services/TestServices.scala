package services

import java.time.ZonedDateTime

import _root_.io.circe.syntax._
import auth.dto.User
import cats.effect.Effect
import cats.InvariantMonoidal
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import tsec.authentication._


class TestServices[F[_], A](authentiator: Authenticator[F, User, User, A])(implicit F: Effect[F], m: InvariantMonoidal[F]) {

  implicit val dsl = Http4sDsl[F]

  import dsl._

  /*
  Now from here, if want want to create services, we simply use the following
  (Note: Since the type of the service is HttpService[F], we can mount it like any other endpoint!):
   */
  val authedTestService: TSecAuthService[User, A, F] =
    TSecAuthService {
      //Where user is the case class User above
      case request@GET -> Root / "api" / "test" asAuthed user =>
        /*
      Note: The request is of type: SecuredRequest, which carries:
      1. The request
      2. The Authenticator (i.e token)
      3. The identity (i.e in this case, User)
         */

        Ok.apply(s"tested at ${ZonedDateTime.now()}. User data in cookie: $user".asJson)
    }

}
