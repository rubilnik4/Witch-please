package shared.api.dto.tarot.cardsOfDay

import shared.api.dto.tarot.photo.PhotoRequest
import zio.json.*
import zio.schema.*

import java.util.UUID

trait CardOfDayRequest {
  def cardId: UUID
  def description: String
}