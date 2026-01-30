package tarot.models

import java.util.UUID

final case class TestChannelState(
  userId: Option[UUID],
  token: Option[String],
  userChannelId: Option[UUID]                               
)

object TestChannelState {
  val empty: TestChannelState =
    TestChannelState(None, None, None)
    
  extension (state: TestChannelState)
    def withUserId(value: UUID): TestChannelState =
      state.copy(userId = Some(value))

    def withToken(value: String): TestChannelState =
      state.copy(token = Some(value))

    def withUserChannel(value: UUID): TestChannelState =
      state.copy(userChannelId = Some(value))  
}