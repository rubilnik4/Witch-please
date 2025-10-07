package shared.api.dto.telegram

import zio.json.*
import zio.json.ast.Json
import zio.json.internal.Write
import zio.schema.*

sealed trait TelegramKeyboardMarkup derives Schema

final case class TelegramReplyKeyboardMarkup(
  @jsonField("keyboard") keyboard: List[List[TelegramKeyboardButton]],
  @jsonField("resize_keyboard") resizeKeyboard: Option[Boolean] = Some(true),
  @jsonField("one_time_keyboard") oneTimeKeyboard: Option[Boolean] = Some(false),
  @jsonField("input_field_placeholder") inputFieldPlaceholder: Option[String] = None,
  @jsonField("selective") selective: Option[Boolean] = None
) extends TelegramKeyboardMarkup derives JsonCodec, Schema

final case class TelegramInlineKeyboardMarkup(
  @jsonField("inline_keyboard") inlineKeyboard: List[List[TelegramInlineKeyboardButton]]
) extends TelegramKeyboardMarkup derives JsonCodec, Schema

final case class TelegramForceReply(
  @jsonField("force_reply") forceReply: Boolean = true,
  @jsonField("input_field_placeholder") inputFieldPlaceholder: Option[String] = None,
  @jsonField("selective")selective: Option[Boolean] = None
) extends TelegramKeyboardMarkup derives JsonCodec, Schema

object TelegramKeyboardMarkup {
  given JsonEncoder[TelegramKeyboardMarkup] =
    (keyboardMarkup: TelegramKeyboardMarkup, indent: Option[Int], out: Write) => keyboardMarkup match {
      case reply: TelegramReplyKeyboardMarkup =>
        JsonEncoder[TelegramReplyKeyboardMarkup].unsafeEncode(reply, indent, out)
      case inline: TelegramInlineKeyboardMarkup =>
        JsonEncoder[TelegramInlineKeyboardMarkup].unsafeEncode(inline, indent, out)
      case force: TelegramForceReply =>
        JsonEncoder[TelegramForceReply].unsafeEncode(force, indent, out)
  }

  given JsonDecoder[TelegramKeyboardMarkup] =
    JsonDecoder[Json.Obj].mapOrFail { obj =>
      val keys = obj.fields.map(_._1).toSet
      keys match {
        case ks if ks.contains("force_reply") =>
          obj.as[TelegramForceReply].left.map(err => s"ForceReply decode failed: $err")
        case ks if ks.contains("inline_keyboard") =>
          obj.as[TelegramInlineKeyboardMarkup].left.map(err => s"InlineKeyboard decode failed: $err")
        case ks if ks.contains("keyboard") =>
          obj.as[TelegramReplyKeyboardMarkup].left.map(err => s"ReplyKeyboard decode failed: $err")
        case unknown =>
          Left(s"Unknown TelegramKeyboardMarkup shape, keys = $unknown")
      }
    }
}


