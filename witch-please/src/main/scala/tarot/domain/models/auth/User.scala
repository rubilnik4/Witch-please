package tarot.domain.models.auth

final case class User(
  userId: UserId,
  clientType: ClientType,
  project: String,
  secretHash: String,
  role: UserRole
)
