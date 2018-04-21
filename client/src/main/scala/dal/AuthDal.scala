package dal

import java.util.UUID

import auth.dto.{SignInUpData, UserInfo}
import http.httpClient._
import io.circe.syntax._
import japgolly.scalajs.react.extra.Ajax
import misc.SharedVariables.uuidUrlParamKey

object AuthDal {
  def signIn(data: SignInUpData): OnComplete[UserInfo] =
    jsonResponse(Ajax.post("/api/auth/sign-in").setRequestContentTypeJson.send(data.asJson.noSpaces))

  def signUp(data: SignInUpData): OnComplete[Unit] =
    noResponse(Ajax.post("/api/auth/sign-up").setRequestContentTypeJson.send(data.asJson.noSpaces))

  def signOut: OnComplete[Unit] =
    noResponse(Ajax.post("/api/auth/sign-out").send)

  //TODO: Error ADTs
  def activate(activationId: UUID): OnComplete[String] =
    transform(withResponse(Ajax.post(s"/api/auth/activation?$uuidUrlParamKey=$activationId").send))(_.responseText)
}
