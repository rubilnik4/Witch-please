package common.models.telegram

final case class TelegramFile(
  fileName: String,
  bytes: Array[Byte]
)
