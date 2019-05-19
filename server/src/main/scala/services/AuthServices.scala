package services

import java.time.Instant
import java.util.UUID

import auth.dto.{SignInUpData, UserInfo}
import cats.Monad
import cats.data.EitherT
import cats.effect.Effect
import org.http4s.{HttpRoutes, Response, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import io.circe.syntax._
import cats.syntax.functor._
import cats.syntax.flatMap._
import dal.{SignUpDal, UserDal, UserRecord}
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import tsec.authentication._
import utils.Instances.uuidParamDecoder
import misc.SharedVariables.uuidUrlParamKey
import misc.SharedAliases.Email
import services.AuthServices.ActivationPath
import EitherT.liftF

object AuthServices {
  type ActivationPath = Uri
}
class AuthServices[F[_], A](authentiator: Authenticator[F, UserInfo, UserInfo, A],
                            signInResponsePostProcess: (A, Response[F]) => Response[F],
                            userDal: UserDal[F],
                            signUpDal: SignUpDal[F],
                            sendActivationEmail: (Email, ActivationPath) => F[Unit])(
    implicit F: Effect[F],
    m: Monad[F]
) {
  implicit val decoder = jsonOf[F, SignInUpData]
  implicit val dsl = Http4sDsl[F]

  object UuidVal extends QueryParamDecoderMatcher[UUID](uuidUrlParamKey)

  import dsl._

  private def signInResponse(authedUser: UserInfo): F[Response[F]] =
    // User goes into subject of a token.Client will provide it each time he calls APIs.
    for {
      now <- F.delay(Instant.now())
      authenticator <- authentiator.create(authedUser)
      r <- Ok(authedUser.asJson).map(signInResponsePostProcess(authenticator, _))
    } yield r

  val signInUpService: HttpRoutes[F] = {

    HttpRoutes.of[F] {
      //Where user is the case class User above
      case request @ POST -> Root / "api" / "auth" / "sign-in" =>
        for {
          //check what statius does it retun if ecoding fails. (in case of 500 use attemptAs)
          sd <- request.as[SignInUpData]
          usrOpt <- userDal.verifyUser(sd.email, sd.password)
          r <- usrOpt
            .map(usr => signInResponse(UserInfo(usr.name, usr.email)))
            .getOrElse(m.point(Response[F](Unauthorized)))
        } yield r

      // if else(nested) instead of (plain)Either, reason: same output type and only one branching
      case request @ POST -> Root / "api" / "auth" / "sign-up" =>
        for {
          sd <- request.as[SignInUpData]
          exists <- userDal.exists(sd.email)
          res <- if (exists) Conflict()
          else
            for {
              uuid <- signUpDal.create(sd.email, sd.password)
              _ <- sendActivationEmail(sd.email, Uri(path = s"#sign-in/$uuid"))
            } yield Response[F](Ok)
        } yield res

      //TODO: Introduce ADT-like errors in payload response in addition to codes (this will give compile time safety and better documentation).
      //structure : 200 - Result type , non-200-th -error ADT
      case request @ POST -> Root / "api" / "auth" / "activation" :? UuidVal(uuid) =>
        val res: EitherT[F, Response[F], Response[F]] = for {
          sd <- EitherT(signUpDal.get(uuid).map(_.toRight(Response[F](NotFound))))
          _ <- EitherT(userDal.exists(sd.email).map(v => Either.cond(!v, (), Response[F](Conflict))))
          _ <- liftF(signUpDal.delete(uuid))
          // user will fill in name using profile editing view after registration
          _ <- liftF(userDal.create(UserRecord("", sd.email, sd.password)))
          ok <- liftF(Ok(sd.email))
        } yield ok

        res.merge
    }
  }

  val signOutService: TSecAuthService[UserInfo, A, F] =
    TSecAuthService.apply {
      case request @ POST -> Root / "api" / "auth" / "sign-out" asAuthed user =>
        authentiator.discard(request.authenticator).>>(Ok())
    }
}
