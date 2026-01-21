package bot.application.handlers.telegram.flows

import bot.domain.models.telegram.TelegramContext
import bot.layers.BotEnv
import shared.api.dto.tarot.users.UserResponse
import shared.infrastructure.services.telegram.TelegramApiService
import zio.ZIO

object ChannelFlow {
//  private def showCard(context: TelegramContext, user: UserResponse)(telegramApi: TelegramApiService): ZIO[BotEnv, Throwable, Unit] =
//    val editButton = TelegramInlineKeyboardButton("Изменить", Some(AuthorCommands.cardEdit(card.id)))
//    val deleteButton = TelegramInlineKeyboardButton("Удалить", Some(AuthorCommands.cardDelete(card.id)))
//    val backButton = TelegramInlineKeyboardButton("⬅ К картам", Some(AuthorCommands.spreadCardsSelect(spreadId)))
//    val buttons = List(editButton, deleteButton)
//
//    val summaryText =
//      s""" Настройки канала:
//         | Пользователь: ${user.name}
//         | Идентификатор канала: $cardsPositions
//         | Название канала: $cardOfDayText
//         |
//         |Выбери действие:
//         |""".stripMargin
//    for {
//      _ <- telegramApi.sendInlineButtons(context.chatId, summaryText, buttons)
//    } yield ()
}
