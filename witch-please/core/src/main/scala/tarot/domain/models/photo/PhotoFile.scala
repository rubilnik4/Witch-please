package tarot.domain.models.photo

import shared.models.files.FileSourceType

final case class PhotoFile(
  sourceType: FileSourceType,
  fileId: String
)