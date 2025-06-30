package tarot.infrastructure.services.clients

import tarot.domain.models.TarotError
import zio.{Scope, ZIO}
import zio.json.{JsonDecoder, JsonEncoder}
import zio.json.*
import zio.http.*

object ZIOHttpClient {
  def sendPost[Request: JsonEncoder, Response: JsonDecoder](
      url: URL, body: Request): ZIO[Client & Scope, TarotError, Response] = {
    val request = Request
      .post(url, Body.fromString(body.toJson))
      .setHeaders(Headers(Header.ContentType(MediaType.application.json)))

    for {      
      response <- Client.batched(request)
        .mapError(e => TarotError.ServiceUnavailable("HTTP request failed", e))

      str <- response.body.asString
        .mapError(e => TarotError.ParsingError("body", e.getMessage))

      decoded <- ZIO.fromEither(str.fromJson[Response])
        .mapError(msg => TarotError.ParsingError("response", msg))
    } yield decoded
  }
}
