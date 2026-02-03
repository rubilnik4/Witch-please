package shared.api.dto.telegram

import zio.json.{JsonDecoder, JsonEncoder}

enum TelegramChatMemberStatus:
  case Creator
  case Administrator
  case Member
  case Left
  case Kicked
  case Unknown(status: String)

given JsonEncoder[TelegramChatMemberStatus] =
  JsonEncoder.string.contramap {
    case TelegramChatMemberStatus.Creator => "creator"
    case TelegramChatMemberStatus.Administrator => "administrator"
    case TelegramChatMemberStatus.Member => "member"
    case TelegramChatMemberStatus.Left => "left"
    case TelegramChatMemberStatus.Kicked => "kicked"
    case TelegramChatMemberStatus.Unknown(other) => other
  }

given JsonDecoder[TelegramChatMemberStatus] =
  JsonDecoder.string.map {
    case "creator" => TelegramChatMemberStatus.Creator
    case "administrator" => TelegramChatMemberStatus.Administrator
    case "member" => TelegramChatMemberStatus.Member
    case "left" => TelegramChatMemberStatus.Left
    case "kicked" => TelegramChatMemberStatus.Kicked
    case other => TelegramChatMemberStatus.Unknown(other)
  }  
