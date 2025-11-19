package tarot.domain.models.authorize

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileSource
import shared.models.tarot.authorize.ClientType
import shared.models.tarot.photo.PhotoOwnerType
import tarot.domain.models.cards.{Card, CardId, ExternalCard}
import tarot.domain.models.photo.Photo
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class Author(
  id: UserId,
  name: String,
  spreadsCount: Long
)