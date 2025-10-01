package bot.infrastructure.services.authorize

import zio._
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets.UTF_8

object SecretService {
  private def hmacSha256(data: Array[Byte], key: Array[Byte]): Array[Byte] = {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec(key, "HmacSHA256"))
    mac.doFinal(data)
  }

  def generateSecret(chatId: Long, username: String, pepper: String, version: String = "v1"): UIO[String] =
    ZIO.succeed {
      val key = (pepper + ":" + version).getBytes(UTF_8)
      val payload = s"$chatId:$username".getBytes(UTF_8)
      val raw = hmacSha256(payload, key)
      Base64.getUrlEncoder.withoutPadding().encodeToString(raw)
    }
}
