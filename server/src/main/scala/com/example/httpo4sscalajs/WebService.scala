package com.example.httpo4sscalajs

import java.io.File

import akka.routing.Router
import cats.{Id, InvariantMonoidal}
import cats.effect._
import cats.implicits._
import com.example.httpo4sscalajs.shared.SharedMessages
import fs2.{Scheduler, Stream}
import fs2.io
import org.http4s._
import org.http4s.Http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.twirl._
import java.nio

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.duration._
import cats.effect._
import fs2.Scheduler
import org.http4s._
import org.http4s.dsl.io._


class WebService[F[_]](implicit F: Effect[F], m:InvariantMonoidal[F]) extends Http4sDsl[F] {


  def rootService(
                   implicit scheduler: Scheduler,
                   executionContext: ExecutionContext): HttpService[F] =
    HttpService[F] {

      case GET -> Root =>
        // Supports Play Framework template -- see src/main/twirl.
        Ok.apply(html.index(SharedMessages.itWorks))

      case GET -> Root / "assets"/ name =>
        val str = Option(this.getClass.getClassLoader.getResourceAsStream(s"public/$name"))
        str.map(s => Ok(io.readInputStream[F](m.point(s), 1000)))
        .getOrElse(NotFound("nofo"))

    }




}
