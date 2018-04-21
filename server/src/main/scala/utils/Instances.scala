package utils

import java.util.UUID

import cats.data.{NonEmptyList, Validated}
import org.http4s.{ParseFailure, QueryParamDecoder}

object Instances {
  implicit val uuidParamDecoder: QueryParamDecoder[UUID] =
    v =>
      Validated
        .catchNonFatal(UUID.fromString(v.value))
        .leftMap(t => NonEmptyList.one(ParseFailure(t.getLocalizedMessage, t.toString)))
}
