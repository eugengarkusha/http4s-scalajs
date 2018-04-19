package dal

import auth.dto.{SignInData, User}
import http.httpClient._
import io.circe.syntax._

object AuthDal {
  def signIn(data: SignInData): OnComplete[User] = post("/api/auth/sign-in",  data)
}
