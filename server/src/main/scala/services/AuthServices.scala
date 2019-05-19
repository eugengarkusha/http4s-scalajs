package services

import java.time.Instant
import java.util.UUID

import auth.dto.{SignInUpData, UserInfo}
import cats.Monad
import cats.data.EitherT
import cats.effect.Effect
import org.http4s.{HttpRoutes, Response, Status, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import io.circe.syntax._
import dal.{SignUpDal, SignUpRecord, UserDal, UserRecord}
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import tsec.authentication._
import utils.Instances.uuidParamDecoder
import misc.SharedVariables.uuidUrlParamKey
import misc.SharedAliases.Email
import services.AuthServices.ActivationPath
import EitherT.liftF
import scalaz.zio.{IO, Task, ZIO}
import scalaz.zio.interop.catz._
import cats.implicits._



object AuthServices {
  type ActivationPath = Uri
}
class AuthServices[A](authentiator: Authenticator[Task, UserInfo, UserInfo, A],
                      signInResponsePostProcess: (A, Response[Task]) => Response[Task],
                      userDal: UserDal[Task],
                      signUpDal: SignUpDal[Task],
                      sendActivationEmail: (Email, ActivationPath) => Task[Unit])
{
  implicit val decoder = jsonOf[Task, SignInUpData]
  implicit val dsl = Http4sDsl[Task]

  object UuidVal extends QueryParamDecoderMatcher[UUID](uuidUrlParamKey)

  import dsl._

  private def signInResponse(authedUser: UserInfo): Task[Response[Task]] =
    // User goes into subject of a token.Client will provide it each time he calls APIs.
    for {
      now <- Task(Instant.now())
      authenticator <- authentiator.create(authedUser)
      r <- Ok(authedUser.asJson).map(signInResponsePostProcess(authenticator, _))
    } yield r

  val signInUpService: HttpRoutes[Task] = {

    HttpRoutes.of[Task] {
      //Where user is the case class User above
      case request @ POST -> Root / "api" / "auth" / "sign-in" =>
        for {
          //check what statius does it retun if ecoding fails. (in case of 500 use attemptAs)
          sd <- request.as[SignInUpData]
          usrOpt <- userDal.verifyUser(sd.email, sd.password)
          r <- usrOpt
            .map(usr => signInResponse(UserInfo(usr.name, usr.email)))
            .getOrElse(IO.succeed(Response[Task](Unauthorized)))
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
            } yield Response[Task](Ok)
        } yield res

      //TODO: Introduce ADT-like errors in payload response in addition to codes (this will give compile time safety and better documentation).
      //structure : 200 - Result type , non-200-th -error ADT
      case request @ POST -> Root / "api" / "auth" / "activation" :? UuidVal(uuid) =>
        val res: ZIO[Any, Either[Throwable, Status], Response[Task]] = for {
          sdOpt <- signUpDal.get(uuid).mapError(_.asLeft)
          sd <- ZIO.fromEither(sdOpt.toRight(NotFound.asRight))
          exists <- userDal.exists(sd.email).mapError(_.asLeft)
          _ <- if(exists) ZIO.fail(Conflict.asRight) else ZIO.succeed(())
          _ <- signUpDal.delete(uuid).mapError(_.asLeft)
//          // user will fill in name using profile editing view after registration
          _ <- userDal.create(UserRecord("", sd.email, sd.password)).mapError(_.asLeft)
          ok <- Ok(sd.email).mapError(_.asLeft)
        } yield ok

        res.catchAll(e => ZIO.fromEither(e.map(Response[Task](_))))
    }
  }

  val signOutService: TSecAuthService[UserInfo, A, Task] =
    TSecAuthService.apply {
      case request @ POST -> Root / "api" / "auth" / "sign-out" asAuthed user =>
        authentiator.discard(request.authenticator).>>(Ok())
    }
}
