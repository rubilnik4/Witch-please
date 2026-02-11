package shared.models.photo

import shared.models.files.{FileSourceType, FileStored}

final case class PhotoFile(
  fileStored: FileStored,
  photoSource: PhotoSource
)