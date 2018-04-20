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

object httpClient {

  case class HttpError(code: Int, msg: String)

  private def parseUnsafe[O: Decoder: ClassTag](r: XMLHttpRequest): O =
    parse(r.responseText)
      .flatMap(_.as[O])
      .valueOr(
        err =>
          //it is an unexpected situation if json that we get from server is not parsable/deserializable to expected type
          //the type O serves as a part of protocol between client ans server and we should fail fast is the protocol is broken
          throw new Exception(
            s"Unable to deserialize ${implicitly[ClassTag[O]]} from server." +
              s"Server responded with: Code: ${r.status}, Data: ${r.responseText}, Error: $err"
        ))

  private def validateResp(r: XMLHttpRequest): Either[HttpError, XMLHttpRequest] =
    Either.cond(
      r.status >= 200 && r.status < 300,
      r,
      HttpError(r.status, r.responseText)
    )

  type OnComplete[O] = (Either[HttpError, O] => Callback) => Callback

  private def withResponse[O: Decoder: ClassTag](s2: Ajax.Step2): OnComplete[O] =
    onComplete => s2.onComplete(v => onComplete(validateResp(v).map(parseUnsafe(_)))).asCallback

  private def noResponse(s2: Ajax.Step2): OnComplete[Unit] =
    onComplete => s2.onComplete(v => onComplete(validateResp(v).map(_ => ()))).asCallback

  def post[I: Encoder, O: Decoder: ClassTag](url: String, request: I): OnComplete[O] =
    withResponse(Ajax.post(url).setRequestContentTypeJson.send(request.asJson.noSpaces))

  def post(url: String): OnComplete[Unit] =
    noResponse(Ajax.post(url).send)

  def get[O: Decoder: ClassTag](url: String): OnComplete[O] =
    withResponse(Ajax.get(url).send)

  def put[I: Encoder, O: Decoder: ClassTag](url: String, request: I): OnComplete[O] =
    withResponse(Ajax("PUT", url).setRequestContentTypeJson.send(request.asJson.noSpaces))

}
