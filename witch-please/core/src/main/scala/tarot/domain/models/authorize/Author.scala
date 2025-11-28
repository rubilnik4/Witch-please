package tarot.domain.models.authorize

import zio.UIO

import java.time.Instant
import java.util.UUID

final case class Author(
  id: UserId,
  name: String,
  spreadsCount: Long
)