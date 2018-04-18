package dal

import http.securedHttp.methods._
import http.securedHttp.HttpError

import scala.concurrent.Future

object TestDal {
  import utils.CBT.executionContext

  def test: Future[Either[HttpError, String]] = get[String]("/api/test")
}
