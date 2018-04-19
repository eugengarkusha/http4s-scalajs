package services

import java.time.Instant
import java.time.temporal.ChronoUnit

import auth.dto.{SignInData, User}
import cats.Monad
import cats.data.NonEmptyList
import cats.effect.{Effect, IO}
import org.http4s.{
  AuthScheme,
  Challenge,
  Cookie,
  Credentials,
  DecodeFailure,
  EntityDecoder,
  EntityEncoder,
  Header,
  HttpService,
  Response,
  headers
}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.circe._
import tsec.mac.jca.{HMACSHA256, MacSigningKey}
import io.circe.syntax._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import cats.instances.option._
import cats.instances.either._
import tsec.authentication.Authenticator

class AuthService[F[_], A](authentiator: Authenticator[F, User, User, A],
                           postProcess: (A, Response[F]) => Response[F],
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
      r <- Ok(authedUser.asJson).map(postProcess(authenticator, _))
    } yield r

  val service: HttpService[F] = {
    HttpService {
      //Where user is the case class User above
      case request @ POST -> Root / "api" / "auth" / "sign-in" =>
        for {
          //check what statius does it retun if ecoding fails. (in case of 500 use attemptAs)
          sd <- request.as[SignInData]
          usr <- findUser(null)
          r <- usr
            .map(signInResponse(_))
            .getOrElse(m.point(Response[F](Unauthorized)))
        } yield r
    }
  }
}
