package tarot.infrastructure.services.users

import com.github.roundrop.bcrypt.*
import tarot.domain.models.TarotError
import tarot.domain.models.auth.*
import tarot.domain.models.projects.ProjectId
import tarot.layers.AppEnv
import zio.{Cause, ZIO}

object UserService {
  def hashSecret(clientSecret: String): ZIO[Any, TarotError, String] =
    ZIO.fromTry(clientSecret.bcrypt(12))
      .tapError(err => ZIO.logErrorCause(s"Incryption error", Cause.fail(err)))
      .mapError(err => TarotError.Unauthorized(s"Incryption error"))
}