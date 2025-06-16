package tarot.domain.models.photo

final case class PhotoFile(
    fileName: String, 
    bytes: Array[Byte])