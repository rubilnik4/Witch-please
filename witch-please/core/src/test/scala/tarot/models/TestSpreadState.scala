package tarot.models

import java.util.UUID

final case class TestSpreadState(
  photoId: Option[String],
  userId: Option[UUID],
  token: Option[String],                          
  spreadId: Option[UUID],
  cardIds: Option[List[UUID]],
  cardOfDayId: Option[UUID]
)

object TestSpreadState {
  val empty: TestSpreadState =
    TestSpreadState(None, None, None, None, None, None)
    
  extension (state: TestSpreadState)
    def withPhotoId(value: String): TestSpreadState =
      state.copy(photoId = Some(value))

    def withUserId(value: UUID): TestSpreadState =
      state.copy(userId = Some(value))

    def withToken(value: String): TestSpreadState =
      state.copy(token = Some(value))

    def withSpreadId(value: UUID): TestSpreadState =
      state.copy(spreadId = Some(value))

    def withCardIds(values: List[UUID]): TestSpreadState =
      state.copy(cardIds = Some(values))

    def withCardOfDayId(value: UUID): TestSpreadState =
      state.copy(cardOfDayId = Some(value))
}