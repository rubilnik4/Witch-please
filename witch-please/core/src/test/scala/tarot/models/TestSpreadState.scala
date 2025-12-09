package tarot.models

import java.util.UUID

final case class TestSpreadState(
  photoId: Option[String],
  userId: Option[UUID],
  token: Option[String],                          
  spreadId: Option[UUID],
  cardIds: Option[List[UUID]]
)
