package tarot.infrastructure.services.photo

import sttp.client3.ziojson.asJsonEither
import sttp.client3.*
import sttp.model.MediaType
import tarot.api.dto.telegram.{TelegramErrorResponse, TelegramFileResponse, TelegramSendPhotoResponse}
import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoFile
import zio.*
import zio.json.*

import scala.concurrent.duration.{DurationInt, FiniteDuration}

final class TelegramFileServiceLive(token: String, client: SttpBackend[Task, Any]) extends TelegramFileService:
  private final val baseUrl = s"https://api.telegram.org/bot$token"
  private final val fileBaseUrl = s"https://api.telegram.org/file/bot$token"
  private val timeout: FiniteDuration = 30.seconds

  def downloadPhoto(fileId: String): ZIO[Any, TarotError, PhotoFile] =
    for {
      _ <- ZIO.logDebug(s"Fetching telegram file path for fileId: $fileId")
      fileRequest = getFileRequest(fileId)
      telegramFile <- sendJson(fileRequest)

      filePath = telegramFile.result.filePath
      fileName = filePath.split("/").lastOption.getOrElse(s"${telegramFile.result.fileId}.jpg")

      _ <- ZIO.logDebug(s"Downloading telegram image from path: $filePath")
      imageRequest = getImageRequest(filePath)
      telegramImage <- sendBytes(imageRequest)
    } yield PhotoFile(fileName, telegramImage)

  def sendPhoto(chatId: String, photo: PhotoFile): ZIO[Any, TarotError, String] = {
    for {
      _ <- ZIO.logDebug(s"Sending telegram file ${photo.fileName} to chat $chatId")
      imageRequest = getSendImageRequest(chatId, photo)
      response <- sendJson(imageRequest)
      fileId <- ZIO
        .fromOption(response.result.photo.lastOption.map(_.fileId))
        .tapError(e =>
          ZIO.logError(s"No photo id found for file ${photo.fileName}"))
        .orElseFail(TarotError.ApiError("Telegram API error", 500, s"No photo id found for file ${photo.fileName}"))
    } yield fileId
  }

  private def sendJson[A](
      request: RequestT[Identity, Either[ResponseException[TelegramErrorResponse, String], A], Any]
      ): ZIO[Any, TarotError, A] =
    for {
      response <- client.send(request)
        .tapError(e =>
          ZIO.logErrorCause(s"Failed to send telegram json request", Cause.fail(e)))
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
          ZIO.logErrorCause(s"Failed to send telegram bytes request", Cause.fail(e)))
        .mapError(e => TarotError.ServiceUnavailable(s"Failed to send telegram bytes request", e))

      bytes <- ZIO.fromEither(response.body)
        .tapError(e =>
          ZIO.logErrorCause(s"Failed to download bytes", Cause.fail(e)))
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

  private def getSendImageRequest(chatId: String, photo: PhotoFile) = {
    val uri = uri"$baseUrl/sendPhoto"
    basicRequest
      .post(uri)
      .readTimeout(timeout)
      .multipartBody(
        multipart("chat_id", chatId),
        multipart("photo", photo.bytes)
          .fileName(photo.fileName)
          .contentType(MediaType.ImageJpeg)
      )
      .response(asJsonEither[TelegramErrorResponse, TelegramSendPhotoResponse])
  }
