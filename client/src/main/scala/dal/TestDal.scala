package dal

import http.httpClient._

object TestDal {
  def test: OnComplete[String] = get("/api/test")
}
