package tarot.domain.models.photo

import zio.json.JsonCodec

sealed trait PhotoSource derives JsonCodec

object PhotoSource:
  final case class Telegram(fileId: String) extends PhotoSource derives JsonCodec
  final case class Local(path: String) extends PhotoSource derives JsonCodec
  final case class S3(bucket: String, key: String) extends PhotoSource derives JsonCodec
