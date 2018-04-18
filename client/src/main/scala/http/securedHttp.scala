package http

import io.circe.{Decoder, Encoder}
import io.circe.parser.parse
import org.scalajs.dom
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.{Ajax, AjaxException}
import cats.syntax.either._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import io.circe.syntax._
import dom.window.localStorage


//TODO: think how to do local storage side effects in safer way
object securedHttp {

  private val authHeader = "Authorization"
  private val authPrefix = "Bearer "

  private val tokenKey = "token"

  private val commonHeaders: Map[String, String] =
    Map("Content-Type" -> "application/json")

  // Doing uncontrolled side effects (need to store token in localStorage as per best practices)
  private def tokenHeader: Option[(String, String)] =
    Option(localStorage.getItem(tokenKey)).map(t => authHeader -> (authPrefix + t))

  // Doing uncontrolled side effects (need to store token in localStorage as per best practices)
  def setTokenFromAuthResp(req: XMLHttpRequest): Unit = {
    val header = Option(req.getResponseHeader(authHeader))
      .getOrElse(throw new AssertionError("Could not find auth token in http response"))
    localStorage.setItem(tokenKey, header.stripPrefix(authPrefix))
  }

  def headers: Map[String, String] = commonHeaders ++ tokenHeader

  def foldResp[R](response: Future[XMLHttpRequest])(onSucc: XMLHttpRequest => R, onFail: XMLHttpRequest => R)(implicit ec: ExecutionContext): Future[R] = {
    //throws on all non-200 codes
    response.map(onSucc).recover { case e @ AjaxException(req) => onFail(req) }
  }

  def parserResponse[O: Decoder: ClassTag](response: XMLHttpRequest): O =
  //it is an unexpected situation if json that we get from server is not parsable/deserializable to expected type
  //the type O serves as a part of protocol between client ans server and we should fail fast is the protocol is broken
    parse(response.responseText).flatMap(_.as[O])
      .valueOr(err =>
        throw new Exception(
          s"Unable to deserialize ${implicitly[ClassTag[O]]} from server." +
            s"Server responded with: Code: ${response.status}, Data: ${response.responseText}, Error: $err"
        ))

  case class HttpError(code: Int, msg: String)

  def forFuture[O: Decoder: ClassTag](request: Future[XMLHttpRequest])(implicit ec: ExecutionContext): Future[Either[HttpError, O]] = {
    foldResp(request)(r => Right(parserResponse(r)), r => Left(HttpError(r.status, r.responseText)))
  }

//  class HttpResponse[T]{
//    val body: T
//    val headers: Map[STring]
//  }

  object methods {
    import utils.CBT.executionContext
    def post[I: Encoder, O: Decoder: ClassTag](url: String, request: I): Future[Either[HttpError, O]] =
      forFuture(Ajax.post(url = url, data = request.asJson.noSpaces, headers = headers))

    def post[O: Decoder: ClassTag](url: String): Future[Either[HttpError, O]] =
      forFuture(Ajax.post(url = url, headers = headers))

    def get[O: Decoder: ClassTag](url: String): Future[Either[HttpError, O]] =
      forFuture(Ajax.get(url = url, headers = headers))

    def put[I: Encoder, O: Decoder: ClassTag](url: String, request: I): Future[Either[HttpError, O]] =
      forFuture(Ajax.put(url = url, data = request.asJson.noSpaces, headers = headers))
  }

}
