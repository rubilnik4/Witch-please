package tarot.models

import java.util.UUID

final case class TestState(
  photoId: Option[String],
  spreadId: Option[UUID]
)
