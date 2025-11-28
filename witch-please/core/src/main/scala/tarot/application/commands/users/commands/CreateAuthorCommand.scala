package tarot.application.commands.users.commands

import shared.models.tarot.authorize.ClientType

final case class CreateAuthorCommand(
  clientId: String,
  clientType: ClientType,
  clientSecret: String,
  name: String
)