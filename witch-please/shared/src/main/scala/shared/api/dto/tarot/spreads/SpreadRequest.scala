package shared.api.dto.tarot.spreads

import shared.api.dto.tarot.photo.PhotoRequest

trait SpreadRequest {
  def title: String
  def cardCount: Int
  def photo: PhotoRequest
}
