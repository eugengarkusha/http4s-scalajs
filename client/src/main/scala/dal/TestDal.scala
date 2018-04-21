package dal

import http.httpClient._
import japgolly.scalajs.react.extra.Ajax

object TestDal {
  def test: OnComplete[String] = jsonResponse(Ajax.get("/api/test").send)
}
