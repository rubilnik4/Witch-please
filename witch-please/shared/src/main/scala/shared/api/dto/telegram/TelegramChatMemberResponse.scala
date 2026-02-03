package shared.api.dto.telegram

import zio.json.*
import zio.schema.*

import TelegramChatMemberStatus.given

final case class TelegramChatMemberResponse(
  @jsonField("status") status: TelegramChatMemberStatus,
  @jsonField("can_post_messages") canPostMessages: Option[Boolean] = None
) derives JsonCodec, Schema
