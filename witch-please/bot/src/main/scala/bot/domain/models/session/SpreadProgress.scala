package bot.domain.models.session

final case class SpreadProgress(
  cardsCount: Int,
  createdCount: Int,
  createdIndices: Set[Int]
)
