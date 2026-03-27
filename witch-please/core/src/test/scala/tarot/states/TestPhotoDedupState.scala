package tarot.states

import java.util.UUID

final case class TestPhotoDedupState(
  spreadIds: List[UUID] = Nil,
  photoIds: List[UUID] = Nil,
  fileId: Option[UUID] = None
)
