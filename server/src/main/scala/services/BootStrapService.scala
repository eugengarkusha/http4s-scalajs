package services

import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import scalatags.Text.all._
import misc.SharedVariables.bootstrapId
import scalaz.zio.Task
import cats.implicits._
import scalaz.zio.interop.catz._

import scala.concurrent.ExecutionContext

object BootStrapService extends Http4sDsl[Task] {

  private def bootStrap(): Task[Response[Task]] = {

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


  def apply(blockingEc: ExecutionContext) : HttpRoutes[Task] =
    HttpRoutes.of[Task] {
      case GET -> Root => bootStrap()

      case req@GET -> Root / "assets" / name =>
        StaticFile.fromResource(s"/public/$name", blockingEc, Some(req))
        .getOrElse(Response(NotFound))
    }


}
