package utils

import java.util.UUID

import cats.Monoid
import cats.data.{NonEmptyList, Validated}
import japgolly.scalajs.react.Callback
import org.http4s.{ParseFailure, QueryParamDecoder}

object Instances {
  implicit val cbMonoid: Monoid[Callback] = new Monoid[Callback] {
    override def empty: Callback = Callback.empty
    override def combine(x: Callback, y: Callback): Callback = x >> y
  }

}
