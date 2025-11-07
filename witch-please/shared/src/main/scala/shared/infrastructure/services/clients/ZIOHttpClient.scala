package shared.infrastructure.services.clients

import zio.{ZIO, http}
import zio.http.*
import zio.json.*

object ZIOHttpClient {
  def getRequest(url: URL): http.Request =
    getRequestInternal(url, None)

  def getAuthRequest(url: URL, token: String): http.Request =
    getRequestInternal(url, Some(token))
    
  private def getRequestInternal(url: URL, token: Option[String]): http.Request = {
    val headers = getAuthHeaders(token)
    Request
      .get(url)
      .setHeaders(Headers(headers))
  }
  
  def postRequest[Request: JsonEncoder](url: URL, body: Request): http.Request =
    postRequestInternal(url, body, None)

  def postAuthRequest[Request: JsonEncoder](url: URL, body: Request, token: String): http.Request =
    postRequestInternal(url, body, Some(token))

  private def postRequestInternal[Request: JsonEncoder](url: URL, body: Request, token: Option[String]): http.Request = {
    val headers = getAuthHeaders(token)
    Request
      .post(url, Body.fromString(body.toJson))
      .setHeaders(Headers(headers))
  }

  def putRequest[Request: JsonEncoder](url: URL, body: Request): http.Request =
    putRequestInternal(url, body, None)

  def putAuthRequest[Request: JsonEncoder](url: URL, body: Request, token: String): http.Request =
    putRequestInternal(url, body, Some(token))

  private def putRequestInternal[Request: JsonEncoder](url: URL, body: Request, token: Option[String]) = {
    val headers = getAuthHeaders(token)
    Request
      .put(url, Body.fromString(body.toJson))
      .setHeaders(Headers(headers))
  }

  def deleteRequest(url: URL): http.Request =
    deleteRequestInternal(url, None)

  def deleteAuthRequest(url: URL, token: String): http.Request =
    deleteRequestInternal(url, Some(token))

  private def deleteRequestInternal(url: URL, token: Option[String]) = {
    val headers = getAuthHeaders(token)
    Request
      .delete(url)
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
