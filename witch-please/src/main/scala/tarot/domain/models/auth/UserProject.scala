package tarot.domain.models.auth

final case class UserProject(
  user: UserEntity,
  project: ProjectEntity,
  role: UserRole
)
