package tarot.models

import java.util.UUID

final case class TestSpreadState(
  photoId: Option[String],
  spreadId: Option[UUID]
)
