package dal

import java.time.Instant
import java.util.UUID

import auth.dto.{SignInUpData, UserInfo}
import cats.effect.Effect

import scala.collection.concurrent.TrieMap

// email is the unique business key
case class UserRecord(name: String, email: String, passwordHash: String)

class UserDal[F[_]](implicit F: Effect[F]) {
  // TODO: Imlement with DB
  private var cache = TrieMap[String, UserRecord]("a" -> UserRecord("x", "a", "b"))
  def get(email: String): F[Option[UserRecord]] = F.delay(cache.get(email))
  def verifyUser(email: String, passwordHash: String): F[Option[UserRecord]] =
    F.delay(cache.get(email).filter(_.passwordHash == passwordHash))
  def exists(email: String): F[Boolean] = F.delay(cache.contains(email))
  def create(usr: UserRecord): F[Unit] = F.delay(cache.update(usr.email, usr))
}
