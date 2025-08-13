package bot.domain.models.session

import java.time.Instant
import java.util.UUID

final case class BotSession(
  chatId: Long,                         
  clientSecret: String,
  projectId: Option[UUID],
  //photoId: Option[String],
  //wizard: Option[WizardState],
  updatedAt: Instant
) {
  def touch(now: Instant): BotSession = copy(updatedAt = now)
}
