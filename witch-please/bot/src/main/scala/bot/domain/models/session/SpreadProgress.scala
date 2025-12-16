package bot.domain.models.session

final case class SpreadProgress(
  cardsCount: Int,
  createdPositions: Set[CardPosition]
)
