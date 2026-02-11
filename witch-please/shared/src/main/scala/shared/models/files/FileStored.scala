package shared.models.files

import java.util.UUID

enum FileStored:
  case Local(fileId: UUID, path: String)
  case S3(fileId: UUID, bucket: String, key: String)
