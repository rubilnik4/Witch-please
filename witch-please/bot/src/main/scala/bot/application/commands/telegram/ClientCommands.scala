package bot.application.commands.telegram

import java.util.UUID

object ClientCommands {
  final val Prefix = "/client"
  final val Start = s"/${Prefix}_start"
  final val AuthorSelect = s"/${Prefix}_author_select"

  def authorSelect(authorId: UUID): String =
    s"$AuthorSelect $authorId"
}
