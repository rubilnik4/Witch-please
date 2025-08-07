package common.infrastructure.services

import common.api.dto.*
import common.api.dto.telegram.*
import common.models.telegram.*
import sttp.client3.*
import sttp.client3.ziojson.asJsonEither
import sttp.model.{MediaType, Uri}
import zio.*
import zio.json.*

import scala.concurrent.duration.{DurationInt, FiniteDuration}

final class TelegramApiServiceLive(token: String, client: SttpBackend[Task, Any]) extends TelegramApiService:
  private final val baseUrl = s"https://api.telegram.org/bot$token"
  private final val fileBaseUrl = s"https://api.telegram.org/file/bot$token"
  private final val sendMessageUrl = uri"$baseUrl/sendMessage"
  private final val sendPhotoUrl = uri"$baseUrl/sendPhoto"

  def sendText(chatId: Long, text: String): ZIO[Any, TelegramError, Long] = {
    for {
      _ <- ZIO.logDebug(s"Sending text message to chat $chatId: $text")
      messageRequest = getSendTextRequest(chatId, text)
      response <- sendJson(messageRequest)
    } yield response.messageId
  }

  def downloadPhoto(fileId: String): ZIO[Any, TelegramError, TelegramFile] =
    for {
      _ <- ZIO.logDebug(s"Fetching telegram file path for fileId: $fileId")
      fileRequest = getFileRequest(fileId)
      telegramFile <- sendJson(fileRequest)
      filePath = telegramFile.result.filePath
      fileName = filePath.split("/").lastOption.getOrElse(s"${telegramFile.result.fileId}.jpg")

      _ <- ZIO.logDebug(s"Downloading telegram image from path: $filePath")
      imageRequest = getDownloadImageRequest(filePath)
      telegramImage <- sendBytes(imageRequest)
    } yield TelegramFile(fileName, telegramImage)

  def sendPhoto(chatId: Long, fileId: String): ZIO[Any, TelegramError, String] = {
    for {
      _ <- ZIO.logDebug(s"Sending existing photo fileId=$fileId to chat $chatId")
      photoRequest = getSendPhotoRequest(chatId, fileId)
      response <- sendJson(photoRequest)
      fileId <- getPhotoId(response, fileId)
    } yield fileId
  }

  def sendPhoto(chatId: Long, photo: TelegramFile): ZIO[Any, TelegramError, String] = {
    for {
      _ <- ZIO.logDebug(s"Sending telegram file ${photo.fileName} to chat $chatId")
      photoRequest = getSendPhotoRequest(chatId, photo)
      response <- sendJson(photoRequest)
      fileId <- getPhotoId(response, photo.fileName)
    } yield fileId
  }

  private def getPhotoId(response: TelegramPhotoResponse, fileName: String) =
    ZIO.fromOption(response.result.photo.lastOption.map(_.fileId))
      .tapError(_ => ZIO.logError(s"No photo id found for file $fileName"))
      .orElseFail(TelegramError.ApiError(404, s"No photo id found for file $fileName"))

  private def sendJson[A](request: RequestT[Identity, Either[ResponseException[TelegramErrorResponse, String], A], Any])
      : ZIO[Any, TelegramError, A] =
    for {
      response <- client.send(request)
        .tapError(e => ZIO.logErrorCause(s"Failed to send telegram json request", Cause.fail(e)))
        .mapError(e => TelegramError.RequestFailed(s"Failed to send telegram json request", e))

      result <- ZIO.fromEither(response.body).foldZIO(
        {
          case HttpError(apiError: TelegramErrorResponse, _) =>
            ZIO.logError(s"Telegram API error for: ${apiError.errorCode} - ${apiError.description}") *>
              ZIO.fail(TelegramError.ApiError(apiError.errorCode, apiError.description))
          case decodeErr =>
            ZIO.logErrorCause(s"Telegram API unexpected error format", Cause.fail(decodeErr)) *>
              ZIO.fail(TelegramError.InvalidResponse("Telegram API unexpected error format", decodeErr.getMessage))
        },
        response => ZIO.succeed(response)
      )
    } yield result

  private def sendBytes[A](request: RequestT[Identity, Either[String, Array[Byte]], Any])
      : ZIO[Any, TelegramError, Array[Byte]] =
    for {
      response <- client.send(request)
        .tapError(e => ZIO.logErrorCause(s"Failed to send telegram bytes request", Cause.fail(e)))
        .mapError(e => TelegramError.RequestFailed(s"Failed to send telegram bytes request", e))

      bytes <- ZIO.fromEither(response.body)
        .tapError(e => ZIO.logErrorCause(s"Failed to download bytes", Cause.fail(e)))
        .mapError(e => TelegramError.InvalidResponse(s"Failed to download bytes", e))
    } yield bytes

  private def getFileRequest(fileId: String) = {
    val uri = uri"$baseUrl/getFile?file_id=$fileId"
    basicRequest
      .get(uri)
      .response(asJsonEither[TelegramErrorResponse, TelegramFileResultResponse])
  }

  private def getDownloadImageRequest(filePath: String) = {
    val uri = uri"$fileBaseUrl/$filePath"
    basicRequest
      .get(uri)
      .response(asByteArray)
  }

  private def getSendTextRequest(chatId: Long, text: String) = {
    val request = TelegramMessageRequest(chatId, text)
    getPostRequest(sendMessageUrl, request)
      .response(asJsonEither[TelegramErrorResponse, TelegramMessageResponse])
  }

  private def getSendPhotoRequest(chatId: Long, fileId: String) = {
    val request = TelegramPhotoRequest(chatId, fileId)
    getPostRequest(sendPhotoUrl, request)
      .response(asJsonEither[TelegramErrorResponse, TelegramPhotoResponse])
  }

  private def getSendPhotoRequest(chatId: Long, photo: TelegramFile) = {
    basicRequest
      .post(sendPhotoUrl)
      .multipartBody(
        multipart("chat_id", chatId.toString),
        multipart("photo", photo.bytes)
          .fileName(photo.fileName)
          .contentType(MediaType.ImageJpeg)
      )
      .response(asJsonEither[TelegramErrorResponse, TelegramPhotoResponse])
  }

  private def getPostRequest[Request: JsonEncoder](uri: Uri, request: Request) =
    basicRequest
      .post(uri)
      .body(request.toJson)
      .contentType("application/json")
