package tarot.api.dto.tarot.users

import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.users.{Author, User}
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

object AuthorResponseMapper { 
  def toResponse(author: Author): AuthorResponse =
    AuthorResponse(
      id = author.id.id,
      name = author.name,
      spreadsCount = author.spreadsCount,
    )
}