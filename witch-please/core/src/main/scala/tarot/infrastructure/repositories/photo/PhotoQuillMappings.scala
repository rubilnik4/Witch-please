package tarot.infrastructure.repositories.photo

import io.getquill.MappedEncoding
import shared.models.files.*

object PhotoQuillMappings {
  given MappedEncoding[FileSourceType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, FileSourceType] = MappedEncoding(FileSourceType.valueOf)

  given MappedEncoding[FileStoredType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, FileStoredType] = MappedEncoding(FileStoredType.valueOf)
}
