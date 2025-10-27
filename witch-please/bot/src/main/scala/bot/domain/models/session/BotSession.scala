package bot.domain.models.session

import zio.ZIO

import java.time.Instant
import java.util.UUID

final case class BotSession(
  clientSecret: String,
  token: Option[String],
  userId: Option[UUID],
  pending: Option[BotPendingAction],
  projectId: Option[UUID],
  spreadId: Option[UUID],
  spreadProgress: Option[SpreadProgress],
  updatedAt: Instant
)

object BotSession {
  def newSession(clientSecret: String, now: Instant): BotSession =
    BotSession(clientSecret, None, None, None, None, None, None, now)

  def withUser(session: BotSession, userId: UUID, token: String, now: Instant): BotSession =
    session.copy(userId = Some(userId), token = Some(token), updatedAt = now)

  def withPending(session: BotSession, pending: Option[BotPendingAction], now: Instant): BotSession =
    session.copy(pending = pending, updatedAt = now)  

  def withProject(session: BotSession, projectId: UUID, token: String, now: Instant): BotSession =
    session.copy(projectId = Some(projectId), token = Some(token), updatedAt = now)

  def withSpread(session: BotSession, spreadId: UUID, spreadProgress: SpreadProgress, now: Instant): BotSession =
    session.copy(
      spreadId = Some(spreadId),
      spreadProgress = Some(spreadProgress),
      pending = None,
      updatedAt = now)

  def clearSpread(session: BotSession, now: Instant): BotSession =
    session.copy(
      spreadId = None,
      spreadProgress = None,
      pending = None,
      updatedAt = now)

  def clearSpreadProgress(session: BotSession, now: Instant): BotSession =
    session.copy(
      spreadProgress = None,
      pending = None,
      updatedAt = now)
      
  def withCard(session: BotSession, index: Int, now: Instant): ZIO[Any, Throwable, BotSession] =
    for {
      progress <- ZIO.fromOption(session.spreadProgress)
        .orElseFail(new IllegalStateException(s"Cannot add card $index: spreadProgress is empty for userId=${session.userId}"))

      _ <- ZIO.fail(new IllegalArgumentException(s"Card index $index is out of bounds [0, ${progress.cardsCount - 1}]"))
        .unless(index >= 0 && index < progress.cardsCount)

      nextProgress = if (progress.createdIndices.contains(index)) progress
        else progress.copy(
          createdIndices = progress.createdIndices + index)
    } yield session.copy(spreadProgress = Some(nextProgress), pending = None, updatedAt = now)

  def touched(session: BotSession, now: Instant): BotSession =
    session.copy(updatedAt = now)
}