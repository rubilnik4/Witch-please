package shared.infrastructure.services.clients

import zio.{ZIO, http}
import zio.http.*
import zio.json.*

object ZIOHttpClient {
  def getPostRequest[Request: JsonEncoder](url: URL, body: Request): http.Request =
    getPostRequestInternal(url, body, None)

  def getAuthPostRequest[Request: JsonEncoder](url: URL, body: Request, token: String): http.Request =
    getPostRequestInternal(url, body, Some(token))

  private def getPostRequestInternal[Request: JsonEncoder](url: URL, body: Request, token: Option[String]): http.Request = {
    val headers = getAuthHeaders(token)
    Request
      .post(url, Body.fromString(body.toJson))
      .setHeaders(Headers(headers))
  }

  def getPutRequest[Request: JsonEncoder](url: URL, body: Request): http.Request =
    getPutRequestInternal(url, body, None)

  def getAuthPutRequest[Request: JsonEncoder](url: URL, body: Request, token: String): http.Request =
    getPutRequestInternal(url, body, Some(token))

  private def getPutRequestInternal[Request: JsonEncoder](url: URL, body: Request, token: Option[String]) = {
    val headers = getAuthHeaders(token)
    Request
      .put(url, Body.fromString(body.toJson))
      .setHeaders(Headers(headers))
  }

  private def getAuthHeaders(token: Option[String]) =
    val baseHeaders = List(Header.ContentType(MediaType.application.json))
    val headers = token match {
      case Some(token) => baseHeaders :+ Header.Authorization.Bearer(token)
      case None => baseHeaders
    }
    headers

  def getResponse[Response: JsonDecoder](response: http.Response): ZIO[Any, Throwable, Response] =
    for {
      strResponse <- response.body.asString
        .mapError(th => new RuntimeException(s"Failed to read response body: ${th.getMessage}"))

      parsed <- ZIO
        .fromEither(strResponse.fromJson[Response])
        .mapError(msg => new RuntimeException(s"Invalid JSON: $msg"))
    } yield parsed
}
