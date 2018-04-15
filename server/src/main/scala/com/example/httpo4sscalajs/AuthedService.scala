package com.example.httpo4sscalajs

import auth.dto.{SignInData, User}
import cats.data.OptionT
import cats.{Applicative, Functor, Id, InvariantMonoidal, Monad}
import tsec.mac.jca.{HMACSHA256, MacErrorM, MacSigningKey}
import cats.effect.Effect
import com.example.httpo4sscalajs.shared.TestToken
import org.http4s.HttpService
import tsec.authentication._
import tsec.common.SecureRandomId
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s._
import org.http4s.Http4s._
import org.http4s.dsl._
import cats.syntax.traverse._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.instances.either._
import cats.instances.option._
import tsec.jws.mac.{JWTMac, JWTMacImpure}
import tsec.jwt.JWTClaims
import _root_.io.circe.syntax._

import scala.concurrent.duration._


class AuthService[F[_]](findUser: SignInData => F[Option[User]])(implicit F: Effect[F], m: Monad[F]) {
  implicit val dsl = Http4sDsl[F]
  import dsl._

  val key: MacSigningKey[HMACSHA256] = HMACSHA256.unsafeGenerateKey

  def mkTokenHeader(u:User): F[Authorization] ={
    JWTMac.build[F, HMACSHA256](JWTClaims(subject = Some(u.asJson.noSpaces)), key).map(token =>
      Authorization(
        Credentials.Token(
          AuthScheme.Bearer,
          token.toEncodedString
        )
      )
    )
  }



  val service: HttpService[F] = HttpService{
    //Where user is the case class User above
    case request@GET -> Root / "api" /"auth"/"sign-in" =>
      val k: F[Either[DecodeFailure, Option[User]]] = request.attemptAs[SignInData].value.flatMap(_.traverse(findUser(_)))
      k.flatMap(e =>
        e.fold(
         df =>  BadRequest(df.getLocalizedMessage),
          _.map(mkTokenHeader(_).flatMap(Ok(_))).getOrElse(Unauthorized())
        )
      )

  }


}
class AuthedService[F[_]](implicit F: Effect[F], m:InvariantMonoidal[F]) {

  implicit val dsl = Http4sDsl[F]
  import dsl._


///
//    import http4sExamples.ExampleAuthHelpers._

  //keeping user info in token
  //this is the dependency
    val userStore = new IdentityStore[F, User,  User]{
      override def get(u: User): OptionT[F, User] = OptionT.pure(u)
    }
//      dummyBackingStore[IO, SecureRandomId, AugmentedJWT[HMACSHA256, Int]](s => SecureRandomId.coerce(s.id))

    //We create a way to store our users. You can attach this to say, your doobie accessor
//    val userStore: BackingStore[IO, Int, User] = dummyBackingStore[IO, Int, User](_.id)

//    val signingKey
//    : MacSigningKey[HMACSHA256] = HMACSHA256.unsafeGenerateKey //Our signing key. Instantiate in a safe way using GenerateLift
//    val zz: IO[MacSigningKey[HMACSHA256]] = HMACSHA256.generateKey[IO]




  //this is the dependency
    val Auth: SecuredRequestHandler[F, User, User, AugmentedJWT[HMACSHA256, User]] ={
      val jwtStatelessAuth: JWTAuthenticator[F, User, User, HMACSHA256] =
        JWTAuthenticator.unbacked.inBearerToken(
          expiryDuration = 10.minutes, //Absolute expiration time
          maxIdle        = None,
          identityStore  = userStore,
          signingKey     = TestToken.key
        )
      SecuredRequestHandler.apply(jwtStatelessAuth)
    }


    /*
    Now from here, if want want to create services, we simply use the following
    (Note: Since the type of the service is HttpService[F], we can mount it like any other endpoint!):
     */
    val service: HttpService[F] = Auth.apply{
      //Where user is the case class User above
      case request@GET -> Root / "api" /"test" asAuthed user =>
        /*
        Note: The request is of type: SecuredRequest, which carries:
        1. The request
        2. The Authenticator (i.e token)
        3. The identity (i.e in this case, User)
         */
//        val r: SecuredRequest[F, User, AugmentedJWT[HMACSHA256, Int]] = request
        val r: SecuredRequest[F, User, AugmentedJWT[HMACSHA256, Int]] = request
        println("TOKEN ID "+r.authenticator.id)
        println("USER "+r.identity)
        Ok.apply("WORKS")

    }



}
