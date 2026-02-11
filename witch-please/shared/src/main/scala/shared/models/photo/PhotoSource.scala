package shared.models.photo

import shared.models.files.FileSourceType

final case class PhotoSource(
  sourceId: String,
  sourceType: FileSourceType,
  parentId: Option[String]                          
)