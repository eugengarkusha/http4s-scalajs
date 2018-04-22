package dal

import java.time.Instant
import java.util.UUID

import cats.effect.Effect

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.FiniteDuration
import cats.syntax.flatMap._
import cats.syntax.functor._

case class SignUpRecord(id: UUID, email: String, password: String, created: Instant = Instant.now())

class SignUpDal[F[_]](implicit F: Effect[F]) {
  // TODO: Imlement with DB
  private var cache = TrieMap.empty[UUID, SignUpRecord]

  def create(email: String, passwordHash: String): F[UUID] = F.delay {
    val id = UUID.randomUUID()
    cache.update(id, SignUpRecord(id, email, passwordHash))
    id
  }
  def get(id: UUID): F[Option[SignUpRecord]] = F.delay(cache.get(id))
  def delete(id: UUID): F[Unit] = F.delay(cache.remove(id))

  def deleteOlderThan(d: FiniteDuration): F[List[SignUpRecord]] =
    for {
      t <- F.delay(Instant.now().minusSeconds(d.toSeconds))
      r <- F.delay(cache.flatMap { case (id, sr) => if (sr.created.isBefore(t)) cache.remove(id) else None }(
        collection.breakOut): List[SignUpRecord])
    } yield r

}
