package services

import tsec.mac.jca.{HMACSHA256, MacSigningKey}

// provide key as the dependency on startup
object TestKeyHolder {
  val key: MacSigningKey[HMACSHA256] = HMACSHA256.unsafeGenerateKey
}
