package shared.models.files

enum FileStorage:
  case Local(path: String)
  case S3(bucket: String, key: String)
