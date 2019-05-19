package services

import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import scalatags.Text.all._
import misc.SharedVariables.bootstrapId

import scala.concurrent.ExecutionContext

class BootStrapService[F[_]](blockingEc: ExecutionContext)(implicit F: Effect[F], cs: ContextShift[F]) extends Http4sDsl[F] {

  private def bootStrap(): F[Response[F]] = {

    def pathToBundleAssetName(projectName: String): Either[String, String] = {
      val name = projectName.toLowerCase
      val bundleNames = Seq("-opt-bundle.js", "-fastopt-bundle.js").map(name + )
      bundleNames.filter(dn => getClass.getResource("/public/" + dn) != null) match {
        case Seq(bundleName) => Right(bundleName)
        case bundleAssetNames     => Left(s"expected to have exactly one js app asset but got $bundleAssetNames.")
      }
    }

    pathToBundleAssetName("client").fold(
      InternalServerError(_),
      name =>
        Ok.apply(
          html(
            head(
              title := "http4s-scalajs"
            ),
            body(
              div(id := bootstrapId),
              script(src := "/assets/" + name)
            )
          ).render,
          `Content-Type`(MediaType.text.html)
      )
    )
  }


  def service: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root => bootStrap()

      case req@GET -> Root / "assets" / name =>
        StaticFile.fromResource(s"/public/$name", blockingEc, Some(req))
        .getOrElse(Response(NotFound))
    }


}
