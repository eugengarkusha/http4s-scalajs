package auth.dto

import io.circe.generic.JsonCodec

@JsonCodec
case class UserInfo(name: String, email: String){
  def displayName = if(name.isEmpty) email else name
}
