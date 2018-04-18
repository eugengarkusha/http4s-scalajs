package services

import java.time.ZonedDateTime

import _root_.io.circe.syntax._
import auth.dto.{SignInData, User}
import cats.data.OptionT
import cats.effect.Effect
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{InvariantMonoidal, Monad}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.{HttpService, _}
import tsec.authentication._
import tsec.jws.mac.JWTMac
import tsec.jwt.JWTClaims
import tsec.mac.jca.{HMACSHA256, MacSigningKey}

import scala.concurrent.duration._


class AuthedService[F[_]](implicit F: Effect[F], m:InvariantMonoidal[F]) {

  implicit val dsl = Http4sDsl[F]
  import dsl._


    //keeping user info in token
    //make IdentityStore the dependency
    val userStore = new IdentityStore[F, User,  User]{
      override def get(u: User): OptionT[F, User] = OptionT.pure(u)
    }

  //this is the dependency
    val Auth: SecuredRequestHandler[F, User, User, AugmentedJWT[HMACSHA256, User]] ={
      val jwtStatelessAuth: JWTAuthenticator[F, User, User, HMACSHA256] =
        JWTAuthenticator.unbacked.inBearerToken(
          expiryDuration = 10.minutes, //Absolute expiration time
          maxIdle        = None,
          identityStore  = userStore,
          signingKey     = TestKeyHolder.key
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
        println("TOKEN ID "+request.authenticator.id)
        println("USER "+ user)
        Ok.apply(s"tested at ${ZonedDateTime.now()}. User data in token: $user")

    }



}
