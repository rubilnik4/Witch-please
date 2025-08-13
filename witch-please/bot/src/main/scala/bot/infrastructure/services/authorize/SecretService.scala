package bot.infrastructure.services.authorize

import zio.ZIO

import java.security.SecureRandom
import java.util.Base64

object SecretService {
  private val rng = new SecureRandom()

  def generateSecret(bytes: Int = 32): ZIO[Any, Throwable, String] =
    ZIO.attempt {
      val arr = new Array[Byte](bytes)
      rng.nextBytes(arr)
      Base64.getUrlEncoder.withoutPadding().encodeToString(arr)
    }
}
