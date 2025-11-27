package bot.application.commands.telegram

import java.util.UUID

object AuthorCommands {
  final val Prefix = "/author"
  final val Start = s"${Prefix}_start"
  final val SpreadCreate = s"${Prefix}_spread_create"
  final val SpreadSelect = s"${Prefix}_spread_select"
  final val SpreadCardsSelect = s"${Prefix}_spread_cards_select"
  final val SpreadPublish = s"${Prefix}_spread_publish"
  final val SpreadDelete = s"${Prefix}_spread_delete"
  final val CardCreate = s"${Prefix}_card_create"

  def spreadSelect(spreadId: UUID, cardCount: Int): String =
    s"$SpreadSelect $spreadId $cardCount"

  def spreadCardsSelect(spreadId: UUID): String =
    s"$SpreadCardsSelect $spreadId"

  def spreadPublish(spreadId: UUID): String =
    s"$SpreadPublish $spreadId"

  def spreadDelete(spreadId: UUID): String =
    s"$SpreadDelete $spreadId"

  def cardCreate(index: Int): String =
    s"$CardCreate $index"
}
