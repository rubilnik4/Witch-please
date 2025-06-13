package tarot.domain.models.spreads

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

final case class Spread(id: UUID, title: String, cardCount: Int, 
                        spreadStatus: SpreadStatus, coverPhotoId: String)