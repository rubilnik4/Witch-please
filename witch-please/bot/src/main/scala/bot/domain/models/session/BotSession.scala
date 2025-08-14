package bot.domain.models.session

import java.time.Instant
import java.util.UUID

final case class BotSession(
  clientSecret: String,
  projectId: Option[UUID],
  updatedAt: Instant
)

object BotSession {
  def newSession(clientSecret: String, now: Instant): BotSession =
    BotSession(clientSecret, None, now)

  def withProject(session: BotSession, projectId: UUID, now: Instant): BotSession =
    session.copy(projectId = Some(projectId), updatedAt = now)

  def touched(session: BotSession, now: Instant): BotSession =
    session.copy(updatedAt = now)
}