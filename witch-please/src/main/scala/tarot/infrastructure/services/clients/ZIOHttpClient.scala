package tarot.infrastructure.services.clients

import tarot.domain.models.TarotError
import zio.{Scope, ZIO}
import zio.json.{JsonDecoder, JsonEncoder}
import zio.json.*
import zio.http.*

object ZIOHttpClient {
  def sendPost[Request: JsonEncoder, Response: JsonDecoder](
      url: URL, body: Request): ZIO[Client & Scope, TarotError, Response] = {
    sendPostInternal(url, body, None)
  }

  def sendPostAuth[Request: JsonEncoder, Response: JsonDecoder](
      url: URL, body: Request, token: String): ZIO[Client & Scope, TarotError, Response] = {
    sendPostInternal(url, body, Some(token))
  }

  private def sendPostInternal[Request: JsonEncoder, Response: JsonDecoder](
      url: URL, body: Request, token: Option[String]): ZIO[Client & Scope, TarotError, Response] = {
    for {
      strResponse <- sendPostInternal[Request](url, body, token)
      decoded <- ZIO.fromEither(strResponse.fromJson[Response])
        .mapError(msg => TarotError.ParsingError("response", msg))
    } yield decoded
  }

  private def sendPostInternal[Request: JsonEncoder](url: URL, body: Request, token: Option[String])
      : ZIO[Client & Scope, TarotError, String] = {
    val baseHeaders = List(Header.ContentType(MediaType.application.json))
    val headers = token match {
      case Some(token) => baseHeaders :+ Header.Authorization.Bearer(token)
      case None => baseHeaders
    }
    val request = Request
      .post(url, Body.fromString(body.toJson))
      .setHeaders(Headers(headers))

    for {
      response <- executeResponse(request)
      strResponse <- response.body.asString
        .mapError(e => TarotError.ParsingError("body", e.getMessage))
    } yield strResponse
  }

  def sendPut[Request: JsonEncoder](url: URL, body: Request): ZIO[Client & Scope, TarotError, Unit] = {
    val request = Request
      .put(url, Body.fromString(body.toJson))
      .setHeaders(Headers(Header.ContentType(MediaType.application.json)))

    executeResponse(request).unit
  }

  private def executeResponse(request: Request) =
    Client.batched(request)
      .mapError(e => TarotError.ServiceUnavailable("HTTP request failed", e))
}
