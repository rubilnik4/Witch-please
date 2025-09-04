package shared.models.files

enum FileSource:
  case Local(path: String)
  case S3(bucket: String, key: String)
