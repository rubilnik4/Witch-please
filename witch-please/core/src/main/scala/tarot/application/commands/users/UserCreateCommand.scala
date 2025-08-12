package tarot.application.commands.users

import shared.models.tarot.authorize.ClientType
import tarot.domain.models.authorize.ExternalUser
import tarot.domain.models.cards.ExternalCard


case class UserCreateCommand(externalUser: ExternalUser)
