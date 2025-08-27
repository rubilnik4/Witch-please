package shared.infrastructure.services.clients

import shared.models.api.ApiError
import sttp.client3.*
import sttp.model.{StatusCode, Uri}
import zio.*
import zio.http.URL
import zio.json.*

object SttpClient {
  given ShowError[String] with
    def show(e: String): String = e

  def getPostRequest[Request: JsonEncoder](uri: Uri, request: Request): RequestT[Identity, Either[String, String], Any] =
    basicRequest
      .post(uri)
      .body(request.toJson)
      .contentType("application/json")

  def getPostRequestAuth[Request: JsonEncoder](uri: Uri, request: Request, token: String): RequestT[Identity, Either[String, String], Any] =
    getPostRequest(uri, request)
      .auth.bearer(token)

  def getPutRequest[Request: JsonEncoder](uri: Uri, request: Request): RequestT[Identity, Either[String, String], Any] =
    basicRequest
      .put(uri)
      .body(request.toJson)
      .contentType("application/json")

  def getPutRequestAuth[Request: JsonEncoder](uri: Uri, request: Request, token: String): RequestT[Identity, Either[String, String], Any] =
    getPutRequest(uri, request)
      .auth.bearer(token)

  def sendJson[Response, Error](
      client: SttpBackend[Task, Any],
      request: RequestT[Identity, Either[ResponseException[Error, String], Response], Any])
      : ZIO[Any, ApiError, Response] =
    for {
      response <- client.send(request)
        .tapError(e => ZIO.logErrorCause(s"Failed to send json request", Cause.fail(e)))
        .mapError(e => ApiError.RequestFailed(s"Failed to send json request", e))

      result <- handleResponse(response)
    } yield result

  def sendNoContent[Error](
      client: SttpBackend[Task, Any],
      request: RequestT[Identity, Either[ResponseException[Error, String], Unit], Any])
      : ZIO[Any, ApiError, Unit] =
    for {
      response <- client.send(request)
        .tapError(e => ZIO.logErrorCause("Failed to send no-content request", Cause.fail(e)))
        .mapError(e => ApiError.RequestFailed("Failed to send no-content request", e))

      _ <- handleResponse(response)
    } yield ()

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

  def asJsonNoContent[E: JsonDecoder : IsOption]: ResponseAs[Either[ResponseException[E, String], Unit], Any] =
    asStringAlways.mapWithMetadata { (body, meta) =>
      if (meta.code.isSuccess) {
        Right(())
      } else {
        JsonDecoder[E].decodeJson(body) match {
          case Left(errorString) => Left(DeserializationException(body, errorString))
          case Right(errorDeserialize) => Left(HttpError(errorDeserialize, meta.code))
        }
      }
    }

  def toSttpUri(url: URL): ZIO[Any, ApiError, Uri] =
    ZIO.fromEither(Uri.parse(url.encode))
      .mapError(error => ApiError.RequestFailed(s"Invalid URI", RuntimeException(error)))

  private def handleResponse[E, A](response: Response[Either[ResponseException[E, String], A]]): ZIO[Any, ApiError, A] =
    ZIO.fromEither(response.body).foldZIO(
      {
        case HttpError(body, statusCode) =>
          ZIO.logError(s"API error [${statusCode.code}]: ${body.toString}") *>
            ZIO.fail(ApiError.HttpCode(statusCode.code, body.toString))

        case DeserializationException(body, error) =>
          ZIO.logError(s"Failed to deserialize JSON: $error\nBody: $body") *>
            ZIO.fail(ApiError.InvalidResponse(body, error.toString))
      },
      a => ZIO.succeed(a)
    )
}
