package shared.api.dto.tarot.cards

import shared.api.dto.tarot.photo.PhotoRequest
import zio.json.*
import zio.schema.*

trait CardRequest {
  def title: String
  def description: String
}