package auth.dto

import io.circe.generic.JsonCodec

@JsonCodec
case class SignInUpData(
    email: String,
    password: String
)
