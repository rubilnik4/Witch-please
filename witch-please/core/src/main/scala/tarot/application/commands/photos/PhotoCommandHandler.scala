package tarot.application.commands.photos

import tarot.application.commands.cards.commands.CreateCardCommand
import tarot.domain.models.TarotError
import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.PhotoId
import tarot.layers.TarotEnv
import zio.ZIO

trait PhotoCommandHandler {
  def deletePhoto(photoId: PhotoId): ZIO[TarotEnv, TarotError, Unit]
}
