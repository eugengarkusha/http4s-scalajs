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

  def transform[A, B](oca: OnComplete[A])(f: A => B): OnComplete[B] = ocbf => oca(res => ocbf(res.map(f)))

  def withResponse(s2: Ajax.Step2): OnComplete[XMLHttpRequest] =
    onComplete => s2.onComplete(v => onComplete(validateResp(v))).asCallback

  def jsonResponse[O: Decoder: ClassTag](s2: Ajax.Step2): OnComplete[O] =
    transform(withResponse(s2))(parseUnsafe(_))

  def noResponse(s2: Ajax.Step2): OnComplete[Unit] =
    transform(withResponse(s2))(_ => ())

}
