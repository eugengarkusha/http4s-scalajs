package com.example.httpo4sscalajs

import auth.dto.User
import cats.{Id, InvariantMonoidal}
import cats.data.OptionT
import tsec.mac.jca.{HMACSHA256, MacSigningKey}
import cats.effect.Effect
import com.example.httpo4sscalajs.shared.TestToken
import org.http4s.HttpService
import tsec.authentication._
import tsec.common.SecureRandomId
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s._
import org.http4s.Http4s._
import cats.{Id, InvariantMonoidal}
import org.http4s.dsl._

import scala.concurrent.duration._


class TsecAuthService[F[_]](implicit F: Effect[F], m:InvariantMonoidal[F]) {

  implicit val dsl = Http4sDsl[F]
  import dsl._


///
//    import http4sExamples.ExampleAuthHelpers._

    val userStore = new IdentityStore[F, Int,  User]{
      override def get(id: Int): OptionT[F, User] = OptionT.pure(User(1, "vaha"))
    }
//      dummyBackingStore[IO, SecureRandomId, AugmentedJWT[HMACSHA256, Int]](s => SecureRandomId.coerce(s.id))

    //We create a way to store our users. You can attach this to say, your doobie accessor
//    val userStore: BackingStore[IO, Int, User] = dummyBackingStore[IO, Int, User](_.id)

//    val signingKey
//    : MacSigningKey[HMACSHA256] = HMACSHA256.unsafeGenerateKey //Our signing key. Instantiate in a safe way using GenerateLift
//    val zz: IO[MacSigningKey[HMACSHA256]] = HMACSHA256.generateKey[IO]


    val jwtStatelessAuth: JWTAuthenticator[F, Int, User, HMACSHA256] =
      JWTAuthenticator.unbacked.inBearerToken(
        expiryDuration = 10.minutes, //Absolute expiration time
        maxIdle        = None,
        identityStore  = userStore,
        signingKey     = TestToken.key
      )

    val Auth: SecuredRequestHandler[F, Int, User, AugmentedJWT[HMACSHA256, Int]] =
      SecuredRequestHandler.apply(jwtStatelessAuth)

    /*
    Now from here, if want want to create services, we simply use the following
    (Note: Since the type of the service is HttpService[F], we can mount it like any other endpoint!):
     */
    val service: HttpService[F] = Auth.apply{
      //Where user is the case class User above
      case request@GET -> Root / "api" asAuthed user =>
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
