package com.example.httpo4sscalajs.shared

import tsec.jwt._
import tsec.jws.mac._
import tsec.mac._

import scala.concurrent.duration._
import cats.syntax.all._
import cats.effect.Sync
import tsec.mac.jca.{HMACSHA256, MacSigningKey}
object TestToken {

  val key: MacSigningKey[HMACSHA256] = HMACSHA256.unsafeGenerateKey
  val value = JWTMacImpure.build[HMACSHA256](JWTClaims(subject = Some("1")), key)

//  JWTMac.build[IO, HMACSHA256](
//    JWTClaims(subject = Some("1")), key)
  /** You can interpret into any target Monad with an instance of Sync[F] using JwtMac */
//  def jwtMonadic[F[_]: Sync]: F[JWTMac[HMACSHA256]] =
//    for {
////      key <- HMACSHA256.generateLift[F]
//      jwt <- JWTMac.build[F, HMACSHA256](
//        JWTClaims(subject = Some("1")), key) //You can sign and build a jwt object directly
////      verifiedFromObj <- JWTMac
////        .verifyFromInstance[F, HMACSHA256](jwt, key) //You can verify the jwt straight from an object
////      stringjwt  <- JWTMac.buildToString[F, HMACSHA256](claims, key)       //Or build it straight to string
////      isverified <- JWTMac.verifyFromString[F, HMACSHA256](stringjwt, key) //You can verify straight from a string
////      parsed     <- JWTMac.verifyAndParse[F, HMACSHA256](stringjwt, key)   //Or verify and return the actual instance
//    } yield jwt
  /** Or using an impure either interpreter */
  //  val jwt: Either[Throwable, JWTMac[HMACSHA256]] = for {
  //    key             <- HMACSHA256.generateKey()
  //    jwt             <- JWTMacImpure.build[HMACSHA256](claims, key) //You can sign and build a jwt object directly
  //    verifiedFromObj <- JWTMacImpure.verifyFromInstance[HMACSHA256](jwt, key)
  //    stringjwt       <- JWTMacImpure.buildToString[HMACSHA256](claims, key) //Or build it straight to string
  //    isverified      <- JWTMacImpure.verifyFromString[HMACSHA256](stringjwt, key) //You can verify straight from a string
  //    parsed          <- JWTMacImpure.verifyAndParse[HMACSHA256](stringjwt, key) //Or verify and return the actual instance
  //  } yield parsed
//

}
