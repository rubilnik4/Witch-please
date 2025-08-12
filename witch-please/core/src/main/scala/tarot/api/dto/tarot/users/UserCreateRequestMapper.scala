package tarot.api.dto.tarot.users

import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.authorize.ExternalUser
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

object UserCreateRequestMapper {
  def fromRequest(request: UserCreateRequest, clientType: ClientType): IO[TarotError, ExternalUser] = {
    for {
      _ <- ZIO.fail(ValidationError("clientId must not be empty")).when(request.clientId.isEmpty)
      _ <- ZIO.fail(ValidationError("clientSecret must not be empty")).when(request.clientSecret.isEmpty)
      _ <- ZIO.fail(ValidationError("name must not be empty")).when(request.name.isEmpty)
    } yield toDomain(request, clientType)
  }

  private def toDomain(request: UserCreateRequest, clientType: ClientType): ExternalUser =
    ExternalUser(
      clientId = request.clientId,
      clientSecret = request.clientSecret,
      name = request.name,
      clientType = clientType
    )
}