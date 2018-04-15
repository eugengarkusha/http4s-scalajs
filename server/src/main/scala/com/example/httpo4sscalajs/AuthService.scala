//package com.example.httpo4sscalajs
//
//
//import cats.Monad
//import cats.arrow.Choice
//import cats.effect._
//import cats.implicits._
//import cats.data._
//import org.http4s._
//import org.http4s.dsl.io._
////import org.http4s.implicits._
//import org.http4s.server._
//import org.reactormonk.CryptoBits
//
//case class User(id: Long, name: String)
//class AuthService {
//
////    val authUser:  Kleisli[OptionT[IO, ?], Request[IO], User] = Kleisli(_ => OptionT.liftF(IO(???)))
//  //
//  //  val middleware: AuthMiddleware[IO, User] = AuthMiddleware(authUser)
//  //
//  val authedService: AuthedService[User, IO] =
//  AuthedService {
//    case GET -> Root / "welcome" as user => Ok(s"Welcome, ${user.name}")
//  }
//  //
//  //  val service: HttpService[IO] = middleware(authedService)
//
//  //  val authUser: Kleisli[IO, Request[IO], Either[String, User]] = Kleisli(r => IO(Right(User(1, r.scriptName))))
//
//  import org.http4s.syntax.string._
//  import org.http4s.headers.Authorization
//
//  def retrieveUser: Kleisli[IO, Long, User] = Kleisli(id => IO(???))
//
//  val authUser: Kleisli[IO, Request[IO], Either[String,User]] = Kleisli({ request =>
//
//    val message: Either[String, Long] = for {
//      header <- request.headers.get(Authorization).toRight("Couldn't find an Authorization header")
//      token <- CryptoBits(???).validateSignedToken(header.value).toRight("Invalid token")
//      message <- Either.catchOnly[NumberFormatException](token.toLong).leftMap(_.toString)
//    } yield message
//
//    message.traverse(retrieveUser.run)
//  })
//
//  type J[X]=  OptionT[IO, X]
//  val onFailure: AuthedService[String, IO] = AuthedService[String, IO]{case req => Forbidden(req.authInfo)}
//
//  val m = implicitly[Monad[IO]]
////  type X[M] = {
////    type J[A, B] = Kleisli[OptionT[IO, M], A, B]
////  }
//
//
//  val authorized = AuthMiddleware.apply[IO, String, User](authUser = authUser, onFailure = onFailure)(F=m, C=Kleisli.catsDataChoiceForKleisli[OptionT[IO, ?]])
////
////  val service: HttpService[IO] = authorized(authedService)
//}