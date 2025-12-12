package shared.api.dto.tarot.spreads

import shared.api.dto.tarot.photo.PhotoRequest

trait SpreadRequest {
  def title: String
  def cardCount: Int
  def description: String
  def photo: PhotoRequest
}
