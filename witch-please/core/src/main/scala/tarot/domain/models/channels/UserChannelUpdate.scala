package tarot.domain.models.channels

import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileStored
import shared.models.photo.PhotoSource
import shared.models.tarot.photo.PhotoOwnerType
import tarot.application.commands.cards.commands.UpdateCardCommand
import tarot.application.commands.cardsOfDay.commands.UpdateCardOfDayCommand
import tarot.application.commands.channels.commands.UpdateUserChannelCommand
import tarot.application.commands.spreads.commands.UpdateSpreadCommand
import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.Photo
import tarot.domain.models.spreads.SpreadUpdate
import tarot.domain.models.users.UserId
import zio.UIO

import java.time.Instant
import java.util.UUID

final case class UserChannelUpdate(
  channelId: Long,
  name: String
)

object UserChannelUpdate {
  def toDomain(command: UpdateUserChannelCommand): UserChannelUpdate =
    UserChannelUpdate(
      channelId = command.channelId,
      name = command.name
    )
}
