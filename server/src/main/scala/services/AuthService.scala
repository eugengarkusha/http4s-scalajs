package services

import auth.dto.{SignInData, User}
import cats.Monad
import cats.data.NonEmptyList
import cats.effect.Effect
import org.http4s.{AuthScheme, Challenge, Credentials, DecodeFailure, EntityDecoder, EntityEncoder, HttpService, Response, headers}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.circe._
import tsec.jws.mac.JWTMac
import tsec.jwt.JWTClaims
import tsec.mac.jca.{HMACSHA256, MacSigningKey}
import io.circe.syntax._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import cats.instances.option._
import cats.instances.either._


class AuthService[F[_]](findUser: SignInData => F[Option[User]])(
  implicit F: Effect[F],
  m: Monad[F]
) {
  implicit val decoder = jsonOf[F, SignInData]
  implicit val dsl = Http4sDsl[F]
  import dsl._


  private def signInResponse(authedUser: User): F[Response[F]] =
    // User goes into subject of a token.Client will provide it each time he calls APIs.
    for{
      token <- JWTMac.build[F, HMACSHA256](JWTClaims(subject = Some(authedUser.asJson.noSpaces)), TestKeyHolder.key)
      authHeader = Authorization(
        Credentials.Token(
          AuthScheme.Bearer,
          token.toEncodedString
        )
      )
      r <- Ok(authedUser.asJson, authHeader)
    } yield r


  val service: HttpService[F] = {
    HttpService{
      //Where user is the case class User above
      case request@GET -> Root / "api" /"auth"/"sign-in" =>

      for {
        //check what statius does it retun if ecoding fails. (in case of 500 use attemptAs)
        si <- request.as[SignInData]
        usr <- findUser(si)
        r <- usr.map(signInResponse(_))
         .getOrElse(Unauthorized(
           headers.`WWW-Authenticate`(NonEmptyList.one(Challenge(AuthScheme.Bearer.toString(), "Access to whole APP"))))
         )
      } yield r
    }
  }
}