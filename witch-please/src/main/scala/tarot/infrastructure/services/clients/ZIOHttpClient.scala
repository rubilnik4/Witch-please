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
      response <- executeResponse(request)
      strResponse <- response.body.asString
        .mapError(e => TarotError.ParsingError("body", e.getMessage))
      decoded <- ZIO.fromEither(strResponse.fromJson[Response])
        .mapError(msg => TarotError.ParsingError("response", msg))
    } yield decoded
  }

  def sendPut(url: URL): ZIO[Client & Scope, TarotError, Unit] = {
    val request = Request
      .put(url, Body.empty)
      .setHeaders(Headers(Header.ContentType(MediaType.application.json)))

    executeResponse(request).unit
  }

  private def executeResponse(request: Request) =
    Client.batched(request)
      .mapError(e => TarotError.ServiceUnavailable("HTTP request failed", e))
}
