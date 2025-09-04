package bot.domain.models.session

import java.time.Instant
import java.util.UUID

final case class BotSession(
  clientSecret: String,
  token: Option[String],
  userId: Option[UUID],
  pending: Option[BotPendingAction],
  projectId: Option[UUID],
  spreadId: Option[UUID],
  updatedAt: Instant
)

object BotSession {
  def newSession(clientSecret: String, now: Instant): BotSession =
    BotSession(clientSecret, None, None, None, None, None, now)

  def withUser(session: BotSession, userId: UUID, token: String, now: Instant): BotSession =
    session.copy(userId = Some(userId), token = Some(token), updatedAt = now)

  def withPending(session: BotSession, pending: Option[BotPendingAction], now: Instant): BotSession =
    session.copy(pending = pending, updatedAt = now)  

  def withProject(session: BotSession, projectId: UUID, token: String, now: Instant): BotSession =
    session.copy(projectId = Some(projectId), token = Some(token), updatedAt = now)

  def withSpread(session: BotSession, spreadId: UUID, now: Instant): BotSession =
    session.copy(spreadId = Some(spreadId), updatedAt = now)

  def touched(session: BotSession, now: Instant): BotSession =
    session.copy(updatedAt = now)
}