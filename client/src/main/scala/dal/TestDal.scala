package dal

import http.httpClient.methods._
import http.httpClient.HttpError
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.Ajax

import scala.concurrent.Future

object TestDal {
  import utils.CBT.executionContext

  def test: Future[Either[HttpError, String]] = get[String]("/api/test")

}
