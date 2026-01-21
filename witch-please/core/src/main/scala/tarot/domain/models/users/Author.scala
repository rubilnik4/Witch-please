package tarot.domain.models.users

import tarot.domain.models.users.UserId
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class Author(
  id: UserId,
  name: String,
  spreadsCount: Long
)