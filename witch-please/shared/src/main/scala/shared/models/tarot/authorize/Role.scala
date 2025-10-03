package shared.models.tarot.authorize

import zio.json.*
import sttp.tapir.Schema

enum Role derives JsonCodec, Schema:
  case User, PreProject, Admin

object Role {
  private val level: Map[Role, Int] =
    Map(
      User -> 0,
      PreProject -> 10, 
      Admin -> 100
    )

  def atLeast(actual: Role, required: Role): Boolean =
    level(actual) >= level(required)
}  