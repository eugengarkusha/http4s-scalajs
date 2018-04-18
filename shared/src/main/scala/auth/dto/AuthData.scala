package auth.dto

import io.circe.generic.JsonCodec

/**
  * The form data.
  *
  * @param email      The email of the user.
  * @param password   The password of the user.
  */
@JsonCodec
case class SignInData(
    email: String,
    password: String
)
