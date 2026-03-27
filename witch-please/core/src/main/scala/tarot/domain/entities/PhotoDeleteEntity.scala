package tarot.domain.entities

import java.util.UUID

final case class PhotoDeleteEntity(
  photoId: UUID,
  photoObjectId: UUID,
  fileId: UUID
)
