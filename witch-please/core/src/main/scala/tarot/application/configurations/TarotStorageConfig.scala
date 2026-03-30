package tarot.application.configurations

import shared.application.configurations.S3StorageConfig
import zio.Config
import zio.config.magnolia.deriveConfig

enum TarotStorageType {
  case Local
  case S3
}

object TarotStorageType {
  implicit val config: Config[TarotStorageType] =
    Config.string.mapOrFail {
      case value if value.trim.equalsIgnoreCase("local") => Right(TarotStorageType.Local)
      case value if value.trim.equalsIgnoreCase("s3") => Right(TarotStorageType.S3)
      case value => Left(Config.Error.InvalidData(message = s"Unsupported storage.type '$value', expected 'local' or 's3'"))
    }
}

final case class TarotStorageConfig(
  `type`: TarotStorageType,
  local: Option[LocalStorageConfig],
  s3: Option[S3StorageConfig]
)

object TarotStorageConfig {
  implicit val config: Config[TarotStorageConfig] = deriveConfig[TarotStorageConfig]
}
