package shared.infrastructure.services.clients

import shared.models.api.ApiError
import sttp.client3.*
import sttp.model.{StatusCode, Uri}
import zio.*
import zio.http.URL
import zio.json.*

object SttpClient {
  def getPostRequest[Request: JsonEncoder](uri: Uri, request: Request): RequestT[Identity, Either[String, String], Any] =
    basicRequest
      .post(uri)
      .body(request.toJson)
      .contentType("application/json")

  def sendJson[Response, Error](
      client: SttpBackend[Task, Any],
      request: RequestT[Identity, Either[ResponseException[Error, String], Response], Any])
      : ZIO[Any, ApiError, Response] =
    for {
      response <- client.send(request)
        .tapError(e => ZIO.logErrorCause(s"Failed to send json request", Cause.fail(e)))
        .mapError(e => ApiError.RequestFailed(s"Failed to send json request", e))

      result <- ZIO.fromEither(response.body).foldZIO(
        {
          case HttpError(body, statusCode) =>
            ZIO.logError(s"API error [${statusCode.code}]: ${body.toString}") *>
              ZIO.fail(ApiError.HttpCode(statusCode.code, body.toString))
          case DeserializationException(message, body) =>
            ZIO.logError(s"Failed to deserialize JSON: $message\nBody: $body") *>
              ZIO.fail(ApiError.InvalidResponse("Invalid deserialize JSON", message))
        },
        response => ZIO.succeed(response)
      )
    } yield result

  def sendJsonForBytes[A](client: SttpBackend[Task, Any], request: RequestT[Identity, Either[String, Array[Byte]], Any])
      : ZIO[Any, ApiError, Array[Byte]] =
    for {
      response <- client.send(request)
        .tapError(e => ZIO.logErrorCause(s"Failed to send bytes request", Cause.fail(e)))
        .mapError(e => ApiError.RequestFailed(s"Failed to send bytes request", e))

      bytes <- ZIO.fromEither(response.body)
        .tapError(e => ZIO.logErrorCause(s"Failed to download bytes", Cause.fail(e)))
        .mapError(e => ApiError.InvalidResponse(s"Failed to download bytes", e))
    } yield bytes

  def toSttpUri(url: URL): ZIO[Any, Throwable, Uri] =
    ZIO.fromEither(Uri.parse(url.encode))
      .mapError(error => new RuntimeException(s"Invalid URI: $error"))
}
