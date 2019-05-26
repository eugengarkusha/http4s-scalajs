package services

import java.time.ZonedDateTime

import _root_.io.circe.syntax._
import auth.dto.UserInfo
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import scalaz.zio.Task
import tsec.authentication._
import scalaz.zio.interop.catz._

class TestServices[A] {

  implicit val dsl = Http4sDsl[Task]

  import dsl._

  /*
  Now from here, if want want to create services, we simply use the following
  (Note: Since the type of the service is HttpService[F], we can mount it like any other endpoint!):
   */
  val authedTestService: TSecAuthService[UserInfo, A, Task] =
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

}
