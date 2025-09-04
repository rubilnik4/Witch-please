package shared.models.files

final case class StoredFile(
  fileName: String, 
  bytes: Array[Byte]
)