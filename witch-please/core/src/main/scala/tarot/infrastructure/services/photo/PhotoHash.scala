package tarot.infrastructure.services.photo

import shared.models.files.FileBytes

import java.security.MessageDigest

object PhotoHash {
  def sha256(fileBytes: FileBytes): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest
      .digest(fileBytes.bytes)
      .map("%02x".format(_))
      .mkString
  }
}
