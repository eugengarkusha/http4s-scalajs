package http

import java.nio.ByteBuffer

import cats.syntax.either._
import io.circe.parser.parse
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import japgolly.scalajs.react.Callback
import org.scalajs.dom.XMLHttpRequest

import scala.reflect.ClassTag
import japgolly.scalajs.react.extra.Ajax
import org.scalajs.dom.ext.Ajax.InputData

import scala.scalajs.js
object httpClient {

  case class HttpError(code: Int, msg: String)

  private def parseResp[O: Decoder: ClassTag](r: XMLHttpRequest): Either[HttpError, O] =
    Either.cond(
      r.status >= 200 && r.status < 300,
      //it is an unexpected situation if json that we get from server is not parsable/deserializable to expected type
      //the type O serves as a part of protocol between client ans server and we should fail fast is the protocol is broken
      parse(r.responseText)
        .flatMap(_.as[O])
        .valueOr(
          err =>
            throw new Exception(
              s"Unable to deserialize ${implicitly[ClassTag[O]]} from server." +
                s"Server responded with: Code: ${r.status}, Data: ${r.responseText}, Error: $err"
          )),
      HttpError(r.status, r.responseText)
    )

  type OnComplete[O] = (Either[HttpError, O] => Callback) => Callback

  private def x[O: Decoder: ClassTag](s2: Ajax.Step2): OnComplete[O] =
    onComplete => s2.onComplete(v => onComplete(parseResp(v))).asCallback

  def post[I: Encoder, O: Decoder: ClassTag](url: String, request: I): OnComplete[O] =
    x(Ajax.post(url).setRequestContentTypeJson.send(request.asJson.noSpaces))

  def post(url: String): OnComplete[Unit] =
    x(Ajax.post(url).send)

  def get[O: Decoder: ClassTag](url: String): OnComplete[O] =
    x(Ajax.get(url).send)

  def put[I: Encoder, O: Decoder: ClassTag](url: String, request: I): OnComplete[O] =
    x(Ajax("PUT", url).setRequestContentTypeJson.send(request.asJson.noSpaces))

}
