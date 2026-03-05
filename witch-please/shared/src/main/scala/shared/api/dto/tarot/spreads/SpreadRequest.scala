package shared.api.dto.tarot.spreads

import shared.api.dto.tarot.photo.PhotoRequest

trait SpreadRequest {
  def title: String
  def cardsCount: Int
  def description: String
  def photo: PhotoRequest
}
