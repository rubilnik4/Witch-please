package tarot.domain.models.authorize

case class ExternalUser(
  clientId: String,
  clientType: ClientType,
  clientSecret: String,
  name: String
)
{
  override def toString: String = name
}