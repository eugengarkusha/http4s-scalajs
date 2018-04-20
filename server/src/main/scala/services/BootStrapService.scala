package services

import cats.InvariantMonoidal
import cats.effect._
import fs2.{Scheduler, io}
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import scalatags.Text.all._
import misc.SharedVariables.bootstrapId

import scala.concurrent._

class BootStrapService[F[_]](implicit F: Effect[F], m: InvariantMonoidal[F]) extends Http4sDsl[F] {

  private def bootStrap(): F[Response[F]] = {

    def pathToBundleAsset(projectName: String): Either[String, String] = {
      val name = projectName.toLowerCase
      val bundleNames = Seq(name + "-opt-bundle.js", name + "-fastopt-bundle.js")
      bundleNames.filter(dn => getClass.getResource("/public/" + dn) != null).map("/assets/" + _) match {
        case Seq(bundleAssetPath) => Right(bundleAssetPath)
        case bundleAssetPaths     => Left(s"expected to have excactly one js app asset but got ${bundleAssetPaths}.")
      }
    }

    pathToBundleAsset("client").fold(
      InternalServerError(_),
      path =>
        Ok.apply(
          html(
            head(
              title := "http4s-scalajs"
            ),
            body(
              div(id := bootstrapId),
              script(src := path)
            )
          ).render,
          `Content-Type`(MediaType.`text/html`)
      )
    )
  }

  def service(implicit scheduler: Scheduler, executionContext: ExecutionContext): HttpService[F] =
    HttpService[F] {

      case GET -> Root => bootStrap()

      case GET -> Root / "assets" / name =>
        val str = Option(this.getClass.getClassLoader.getResourceAsStream(s"public/$name"))
        str
          .map(s => Ok(io.readInputStream[F](m.point(s), 1000)))
          .getOrElse(NotFound(s"$name not found"))

    }

}
