package auth.dto

import io.circe.generic.JsonCodec

@JsonCodec
case class SignInUpData(
    email: String,
    passwordHash: String
)