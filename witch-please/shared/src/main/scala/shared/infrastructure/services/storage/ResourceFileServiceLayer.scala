package shared.infrastructure.services.storage

import zio.ZLayer

object ResourceFileServiceLayer {
  val live: ZLayer[Any, Nothing, ResourceFileService] =
    ZLayer.succeed(new ResourceFileServiceLive)
}
