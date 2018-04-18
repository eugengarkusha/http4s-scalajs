package auth.dto

import io.circe.generic.JsonCodec

@JsonCodec
case class User(id: Long, name: String)