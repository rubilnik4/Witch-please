package shared.api.dto.tarot.projects

import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

final case class ProjectCreateRequest(
  name: String
) derives JsonCodec, Schema