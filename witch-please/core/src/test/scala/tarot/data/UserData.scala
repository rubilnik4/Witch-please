package tarot.data

import scala.util.Random

object UserData {
  def generateClientId(): String =
    (1 to 9).map(_ => Random.nextInt(10)).mkString

  def generateClientSecret(length: Int = 32): String = {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    Random.alphanumeric.filter(chars.contains).take(length).mkString
  }  
}
