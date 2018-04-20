package services

import java.time.Instant

import auth.dto.{SignInData, User}
import cats.Monad
import cats.effect.Effect
import org.http4s.{HttpService, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import io.circe.syntax._
import cats.syntax.functor._
import cats.syntax.flatMap._
import tsec.authentication._

class AuthServices[F[_], A](authentiator: Authenticator[F, User, User, A],
                            signInResponsePostProcess: (A, Response[F]) => Response[F],
                            findUser: SignInData => F[Option[User]])(
    implicit F: Effect[F],
    m: Monad[F]
) {
  implicit val decoder = jsonOf[F, SignInData]
  implicit val dsl = Http4sDsl[F]

  import dsl._

  private def signInResponse(authedUser: User): F[Response[F]] =
    // User goes into subject of a token.Client will provide it each time he calls APIs.
    for {
      now <- F.delay(Instant.now())
      authenticator <- authentiator.create(authedUser)
      r <- Ok(authedUser.asJson).map(signInResponsePostProcess(authenticator, _))
    } yield r

  val signInUpService: HttpService[F] = {
    HttpService {
      //Where user is the case class User above
      case request @ POST -> Root / "api" / "auth" / "sign-in" =>
        for {
          //check what statius does it retun if ecoding fails. (in case of 500 use attemptAs)
          sd <- request.as[SignInData]
          usr <- findUser(sd)
          r <- usr
            .map(signInResponse(_))
            .getOrElse(m.point(Response[F](Unauthorized)))
        } yield r
    }
  }

  val signOutService: TSecAuthService[User, A, F] =
    TSecAuthService.apply{
      //Where user is the case class User above
      case request @ POST -> Root / "api" / "auth" / "sign-out" asAuthed user =>
        authentiator.discard(request.authenticator).>>(Ok())
    }
}
