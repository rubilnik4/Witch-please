package tarot.states

import java.util.UUID

final case class TestProjectState(
  userId: Option[UUID],
  token: Option[String],
  projectId: Option[UUID]
)
