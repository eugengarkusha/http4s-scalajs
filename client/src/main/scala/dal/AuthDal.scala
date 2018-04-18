package dal

import auth.dto.{SignInData, User}
import http.securedHttp.methods
import io.circe.syntax._
import org.scalajs.dom.XMLHttpRequest

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import http.securedHttp.{headers => allHeaders, _}
import io.circe.{Decoder, Encoder}
import org.scalajs.dom.ext.Ajax


case class AuthResponse[O](data: O, authToken: String)

object AuthDal {

  import utils.CBT.executionContext

  private def processReq[R: Decoder : ClassTag](r: Future[XMLHttpRequest]): Future[Either[HttpError, R]] = {
    def process(r: XMLHttpRequest): Either[HttpError, R] = Right(parserResponse(r))
    //processing both Ok and notOk responces equally
    foldResp(r)(process, err => Left(HttpError(err.status, err.responseText)))
  }

  def signIn(data: SignInData): Future[Either[String, User]] =
    foldResp(Ajax.post(
      url = "/api/auth/sign-in",
      data = data.asJson.noSpaces,
      headers = allHeaders
      //processing both Ok and notOk responces equally
    ))(
      succ => {
        setTokenFromAuthResp(succ)
        Right(parserResponse[User](succ))
      },
      err => Left(err.status + err.responseText))


}
