package tarot.domain.models.authorize

import shared.models.tarot.authorize.ClientType

case class ExternalUser(
  clientId: String,
  clientType: ClientType,
  clientSecret: String,
  name: String
)
{
  override def toString: String = name
}