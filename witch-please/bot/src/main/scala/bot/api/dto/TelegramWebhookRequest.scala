package bot.api.dto

import zio.json._
import zio.schema._

final case class TelegramWebhookRequest(
  @jsonField("update_id") updateId: Long,
  @jsonField("message") message: Option[TelegramMessageRequest] = None,
  @jsonField("edited_message") editedMessage: Option[TelegramMessageRequest] = None,
  @jsonField("callback_query") callbackQuery: Option[TelegramCallbackQueryRequest] = None,
  @jsonField("inline_query") inlineQuery: Option[TelegramInlineQueryRequest] = None
) derives JsonCodec, Schema

final case class TelegramCallbackQueryRequest(
  @jsonField("id") id: String,
  @jsonField("from") from: TelegramUserRequest,
  @jsonField("message") message: Option[TelegramMessageRequest] = None,
  @jsonField("data") data: Option[String] = None
) derives JsonCodec, Schema

final case class TelegramInlineQueryRequest(
  @jsonField("id") id: String,
  @jsonField("from") from: TelegramUserRequest,
  @jsonField("query") query: String,
  @jsonField("offset") offset: String
)derives JsonCodec, Schema

final case class TelegramMessageRequest(
  @jsonField("message_id") messageId: Long,
  @jsonField("from") from: Option[TelegramUserRequest],
  @jsonField("chat") chat: TelegramChatRequest,
  @jsonField("date") date: Long,
  @jsonField("text") text: Option[String] = None,
  @jsonField("photo") photo: Option[List[TelegramPhotoSizeRequest]] = None
) derives JsonCodec, Schema

final case class TelegramUserRequest(
  @jsonField("id") id: Long,
  @jsonField("is_bot") isBot: Boolean,
  @jsonField("first_name") firstName: String,
  @jsonField("last_name") lastName: Option[String] = None,
  @jsonField("username") username: Option[String] = None,
  @jsonField("language_code") languageCode: Option[String] = None
) derives JsonCodec, Schema

final case class TelegramChatRequest(
  @jsonField("id") id: Long,
  @jsonField("type") `type`: String,
  @jsonField("title") title: Option[String] = None,
  @jsonField("username") username: Option[String] = None,
  @jsonField("first_name") firstName: Option[String] = None,
  @jsonField("last_name") lastName: Option[String] = None
) derives JsonCodec, Schema

final case class TelegramPhotoSizeRequest(
  @jsonField("file_id") fileId: String,
  @jsonField("file_unique_id") fileUniqueId: String,
  @jsonField("width") width: Int,
  @jsonField("height") height: Int,
  @jsonField("file_size") fileSize: Option[Int] = None
) derives JsonCodec, Schema
