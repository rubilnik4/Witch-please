package tarot.api.dto.telegram

import zio.json._
import zio.schema._

final case class TelegramSendPhotoResponse(
    @jsonField("ok") ok: Boolean,
    @jsonField("result") result: TelegramMessage
) derives JsonCodec, Schema

final case class TelegramMessage(
    @jsonField("message_id") messageId: Long,
    @jsonField("chat") chat: TelegramChat,
    @jsonField("date") date: Long,
    @jsonField("photo") photo: List[TelegramPhotoSize]
) derives JsonCodec, Schema

final case class TelegramChat(
    @jsonField("id") id: Long,
    @jsonField("type") `type`: String
) derives JsonCodec, Schema

final case class TelegramPhotoSize(
    @jsonField("file_id") fileId: String,
    @jsonField("file_unique_id") fileUniqueId: String,
    @jsonField("width") width: Int,
    @jsonField("height") height: Int,
    @jsonField("file_size") fileSize: Option[Long]
) derives JsonCodec, Schema
