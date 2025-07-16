package tarot.application.commands.users

import tarot.domain.models.auth.{ClientType, ExternalUser}
import tarot.domain.models.cards.ExternalCard


case class UserCreateCommand(externalUser: ExternalUser)
