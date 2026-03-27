package tarot.application.commands.photos

import java.util.UUID

sealed trait PhotoDeleteResult

object PhotoDeleteResult {
  case object NotFound extends PhotoDeleteResult
  case object DeletedOnlyRecord extends PhotoDeleteResult
  final case class DeletedRecordAndStorage(fileId: UUID) extends PhotoDeleteResult
}
