package services

import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import scalatags.Text.all._
import misc.SharedVariables.bootstrapId

class BootStrapService[F[_]](implicit F: Effect[F]) extends Http4sDsl[F] {

  private def bootStrap(): F[Response[F]] = {

    def pathToBundleAsset(projectName: String): Either[String, String] = {
      val name = projectName.toLowerCase
      val bundleExts = Seq("-opt-bundle.js", "-fastopt-bundle.js").map(name + )
      bundleExts.filter(dn => getClass.getResource("/public/" + dn) != null).map("/assets/" + _) match {
        case Seq(bundleAssetPath) => Right(bundleAssetPath)
        case bundleAssetPaths     => Left(s"expected to have exactly one js app asset but got ${bundleAssetPaths}.")
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


  def service: HttpService[F] =
    HttpService[F] {
      case GET -> Root => bootStrap()

      case req@GET -> Root / "assets" / name =>
        StaticFile.fromResource(s"/public/$name", Some(req))
        .getOrElse(Response(NotFound))
    }


}
