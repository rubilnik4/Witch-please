package bot.domain.models.session

final case class SpreadProgress(
  total: Int,
  createdCount: Int,
  createdIndices: Set[Int]
)
