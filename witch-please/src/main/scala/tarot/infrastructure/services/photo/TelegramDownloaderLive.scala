package tarot.infrastructure.services.photo

import sttp.client3.{SttpBackend, UriContext}
import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoFile
import zio.*
import zio.json.*
import zio.http.*
import zio.http.Client
import sttp.client3.*
import sttp.client3.ziojson.{asJson, asJsonEither}
import sttp.model.Uri
import tarot.api.dto.telegram.{TelegramErrorResponse, TelegramFileResponse}
import zio.*

import java.time.Instant
import scala.concurrent.duration.{DurationInt, FiniteDuration}

final class TelegramDownloaderLive(token: String, client: SttpBackend[Task, Any]) extends TelegramDownloader:
  private final val baseUrl = s"https://api.telegram.org/bot$token"
  private final val fileBaseUrl = s"https://api.telegram.org/file/bot$token"
  private val timeout: FiniteDuration = 30.seconds

  def download(fileId: String): ZIO[Any, TarotError, PhotoFile] =
    for {
      _ <- ZIO.logDebug(s"Fetching telegram file path for fileId=$fileId")
      fileRequest = getFileRequest(fileId)
      telegramFile <- sendJson(fileRequest)

      filePath = telegramFile.result.filePath
      fileName = filePath.split("/").lastOption.getOrElse(s"${telegramFile.result.fileId}.jpg")

      _ <- ZIO.logDebug(s"Downloading telegram image from path: $filePath")
      imageRequest = getImageRequest(filePath)
      telegramImage <- sendBytes(imageRequest)
    } yield PhotoFile(fileName, telegramImage)

  private def sendJson[A](
      request: RequestT[Identity, Either[ResponseException[TelegramErrorResponse, String], A], Any]
      ): ZIO[Any, TarotError, A] =
    for {
      response <- client.send(request)
        .tapError(e =>
          ZIO.logError(s"Failed to send telegram json request. Exception: ${e.getMessage}"))
        .mapError(e => TarotError.ServiceUnavailable(s"Failed to send telegram json request", e))

      result <- ZIO.fromEither(response.body).foldZIO(
        {
          case HttpError(apiError: TelegramErrorResponse, _) =>
            ZIO.logError(s"Telegram API error for: ${apiError.errorCode} - ${apiError.description}") *>
              ZIO.fail(TarotError.ApiError("Telegram API error", apiError.errorCode, apiError.description))
          case decodeErr =>
            ZIO.logErrorCause(s"Telegram API unexpected error format", Cause.fail(decodeErr)) *>
              ZIO.fail(TarotError.ApiError("Telegram API unexpected error format", 500, decodeErr.getMessage))
        },
        response => ZIO.succeed(response)
      )
    } yield result

  private def sendBytes[A](request: RequestT[Identity, Either[String, Array[Byte]], Any])
      : ZIO[Any, TarotError, Array[Byte]] =
    for {
      response <- client.send(request)
        .tapError(e =>
          ZIO.logError(s"Failed to send telegram bytes request. Exception: ${e.getMessage}"))
        .mapError(e => TarotError.ServiceUnavailable(s"Failed to send telegram bytes request", e))

      bytes <- ZIO.fromEither(response.body)
        .tapError(e =>
          ZIO.logError(s"Failed to download bytes. Exception: $e"))
        .mapError(e => TarotError.SerializationError(s"Failed to download bytes: $e"))
    } yield bytes


  private def getFileRequest(fileId: String) = {
    val uri = uri"$baseUrl/getFile?file_id=$fileId"
    basicRequest
      .get(uri)
      .readTimeout(timeout)
      .response(asJsonEither[TelegramErrorResponse, TelegramFileResponse])
  }

  private def getImageRequest(filePath: String) = {
    val uri = uri"$fileBaseUrl/$filePath"
    basicRequest
      .get(uri)
      .readTimeout(timeout)
      .response(asByteArray)
  }
