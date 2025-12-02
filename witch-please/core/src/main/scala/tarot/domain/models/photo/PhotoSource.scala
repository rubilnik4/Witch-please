package tarot.domain.models.photo

import shared.models.files.FileSourceType

final case class PhotoSource(
  sourceType: FileSourceType,
  sourceId: String
)