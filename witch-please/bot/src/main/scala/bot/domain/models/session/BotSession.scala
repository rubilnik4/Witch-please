package bot.domain.models.session

import java.util.UUID

final case class BotSession(
  userId: UUID,
  projectId: Option[UUID],
  photoId: Option[String],
  //wizard: Option[WizardState],
  updatedAt: Instant
)
