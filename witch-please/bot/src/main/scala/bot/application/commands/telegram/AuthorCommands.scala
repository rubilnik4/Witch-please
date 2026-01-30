package bot.application.commands.telegram

import java.util.UUID

object AuthorCommands {
  final val Prefix = "/author"
  final val Start = s"${Prefix}_start"
  final val ChannelCreate = s"${Prefix}_channel_create"
  final val ChannelEdit = s"${Prefix}_channel_edit"
  final val SpreadCreate = s"${Prefix}_spread_create"
  final val SpreadEdit = s"${Prefix}_spread_edit"
  final val SpreadSelect = s"${Prefix}_spread_select"
  final val SpreadCardsSelect = s"${Prefix}_spread_cards_select"
  final val SpreadCardOfDaySelect = s"${Prefix}_spread_cod_select"
  final val SpreadPublish = s"${Prefix}_spread_publish"
  final val SpreadDelete = s"${Prefix}_spread_delete"
  final val CardCreate = s"${Prefix}_card_create"
  final val CardEdit = s"${Prefix}_card_edit"
  final val CardDelete = s"${Prefix}_card_delete"
  final val CardSelect = s"${Prefix}_card_select"
  final val CardOfDayCreate = s"${Prefix}_card_of_day_create"
  final val CardOfDayEdit = s"${Prefix}_card_of_day_edit"
  final val CardOfDayDelete = s"${Prefix}_card_of_day_delete"

  def channelEdit(userChannelId: UUID): String =
    s"$ChannelEdit $userChannelId"
    
  def spreadEdit(spreadId: UUID): String =
    s"$SpreadEdit $spreadId"
    
  def spreadSelect(spreadId: UUID): String =
    s"$SpreadSelect $spreadId"

  def spreadCardsSelect(spreadId: UUID): String =
    s"$SpreadCardsSelect $spreadId"

  def spreadCardOfDaySelect(spreadId: UUID): String =
    s"$SpreadCardOfDaySelect $spreadId"

  def spreadPublish(spreadId: UUID): String =
    s"$SpreadPublish $spreadId"

  def spreadDelete(spreadId: UUID): String =
    s"$SpreadDelete $spreadId"

  def cardCreate(position: Int): String =
    s"$CardCreate $position"

  def cardEdit(cardId: UUID): String =
    s"$CardEdit $cardId"

  def cardDelete(cardId: UUID): String =
    s"$CardDelete $cardId"

  def cardOfDayEdit(cardOfDayId: UUID): String =
    s"$CardOfDayEdit $cardOfDayId"

  def cardOfDayDelete(cardOfDayId: UUID): String =
    s"$CardOfDayDelete $cardOfDayId"  

  def cardSelect(cardId: UUID): String =
    s"$CardSelect $cardId"
}
