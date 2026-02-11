package shared.models.files

final case class FileBytes(
  fileName: String, 
  bytes: Array[Byte]
)