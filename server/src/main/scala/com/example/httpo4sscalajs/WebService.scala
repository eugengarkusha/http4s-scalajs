package com.example.httpo4sscalajs

import java.io.File

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
import java.nio

import org.http4s.headers._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.duration._
import cats.effect._
import fs2.Scheduler
import org.http4s._
import org.http4s.dsl.io._
import scalatags.Text.all._


class WebService[F[_]](implicit F: Effect[F], m:InvariantMonoidal[F]) extends Http4sDsl[F] {


  def bootStrap(): F[Response[F]] = {

    def pathToBundleAsset(projectName: String): Either[String, String] = {
      val name = projectName.toLowerCase
      val bundleNames = Seq(name + "-opt-bundle.js", name + "-fastopt-bundle.js")
      bundleNames.filter(dn => getClass.getResource("/public/" + dn) != null).map("/assets/" + _) match{
        case Seq(bundleAssetPath) => Right(bundleAssetPath)
        case bundleAssetPaths =>  Left(s"expected to have excactly one js app asset but got ${bundleAssetPaths}.")
      }
    }

    pathToBundleAsset("client").fold(
      InternalServerError(_),
      path => Ok.apply(
        html(
          head(
            title:="http4s-scalajs"
          ),
          body(
            h2("Http4s and Scala.js share a same message"),
            ul(
              li("HTTP4s shouts out", em(SharedMessages.itWorks)),
              li("Scala.js  shouts out", em(id := "scalajsShoutOut"))
            ),
            script(src := path)
          )
        ).render,
        `Content-Type`(MediaType.`text/html`)
      )
    )
  }


  def rootService(
                   implicit scheduler: Scheduler,
                   executionContext: ExecutionContext): HttpService[F] =
    HttpService[F] {

      case GET -> Root => bootStrap()

      case GET -> Root / "assets"/ name =>
        val str = Option(this.getClass.getClassLoader.getResourceAsStream(s"public/$name"))
        str.map(s => Ok(io.readInputStream[F](m.point(s), 1000)))
        .getOrElse(NotFound("nofo!"))

    }




}
