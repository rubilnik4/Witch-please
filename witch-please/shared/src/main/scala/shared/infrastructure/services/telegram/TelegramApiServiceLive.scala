package shared.infrastructure.services.telegram

import shared.api.dto.*
import shared.api.dto.telegram.*
import shared.infrastructure.services.clients.SttpClient
import shared.models.api.*
import shared.models.telegram.*
import sttp.client3.*
import sttp.client3.ziojson.asJsonEither
import sttp.model.MediaType
import zio.*
import zio.json.*

final class TelegramApiServiceLive(token: String, client: SttpBackend[Task, Any]) extends TelegramApiService:
  private final val baseUrl = s"https://api.telegram.org/bot$token"
  private final val fileBaseUrl = s"https://api.telegram.org/file/bot$token"
  private final val sendMessageUrl = uri"$baseUrl/sendMessage"
  private final val sendPhotoUrl = uri"$baseUrl/sendPhoto"

  override def sendText(chatId: Long, text: String): ZIO[Any, ApiError, Long] = {
    for {
      _ <- ZIO.logDebug(s"Sending text message to chat $chatId: $text")
      
      request = getSendTextRequest(chatId, text)
      response <- SttpClient.sendJson(client, request)
      textResponse <- TelegramApi.getTelegramResponse(response)
    } yield textResponse.messageId
  }

  override def sendReplyText(chatId: Long, text: String): ZIO[Any, ApiError, Long] = {
    for {
      _ <- ZIO.logDebug(s"Sending reply text message to chat $chatId: $text")
      
      request = getSendMarkupRequest(chatId, text, TelegramForceReply())
      response <- SttpClient.sendJson(client, request)
      textResponse <- TelegramApi.getTelegramResponse(response)
    } yield textResponse.messageId
  }

  override def sendButton(chatId: Long, text: String, button: TelegramKeyboardButton): ZIO[Any, ApiError, Long] =
    sendButtons(chatId, text, List(button))

  override def sendButtons(chatId: Long, text: String, buttons: List[TelegramKeyboardButton]): ZIO[Any, ApiError, Long] = {
    for {
      _ <- ZIO.logDebug(s"Sending button message to chat $chatId: $text")
      
      markup = TelegramReplyKeyboardMarkup(List(buttons))
      request = getSendMarkupRequest(chatId, text, markup)
      response <- SttpClient.sendJson(client, request)
      textResponse <- TelegramApi.getTelegramResponse(response)
    } yield textResponse.messageId
  }

  override def sendInlineButton(chatId: Long, text: String, button: TelegramInlineKeyboardButton): ZIO[Any, ApiError, Long] =
    sendInlineButtons(chatId, text, List(button))

  override def sendInlineButtons(chatId: Long, text: String, buttons: List[TelegramInlineKeyboardButton]): ZIO[Any, ApiError, Long] = 
    sendInlineGroupButtons(chatId, text, buttons.map(List(_)))

  override def sendInlineGroupButtons(chatId: Long, text: String, buttons: List[List[TelegramInlineKeyboardButton]]): ZIO[Any, ApiError, Long] =
    for {
      _ <- ZIO.logDebug(s"Sending inline button message to chat $chatId: $text")
      
      markup = TelegramInlineKeyboardMarkup(buttons)
      request = getSendMarkupRequest(chatId, text, markup)
      response <- SttpClient.sendJson(client, request)
      textResponse <- TelegramApi.getTelegramResponse(response)
    } yield textResponse.messageId

  override def downloadPhoto(fileId: String): ZIO[Any, ApiError, TelegramFile] =
    for {
      _ <- ZIO.logDebug(s"Fetching telegram file path for fileId: $fileId")
      
      fileRequest = getFileRequest(fileId)
      fileResponse <- SttpClient.sendJson(client, fileRequest)
      telegramFile <- TelegramApi.getTelegramResponse(fileResponse)
      fileName = telegramFile.filePath.split("/").lastOption.getOrElse(s"${telegramFile.fileId}.jpg")

      _ <- ZIO.logDebug(s"Downloading telegram image from path: $telegramFile.filePath")
      
      imageRequest = getDownloadImageRequest(telegramFile.filePath)
      telegramImage <- SttpClient.sendJsonForBytes(client, imageRequest)
    } yield TelegramFile(fileName, telegramImage)

  override def sendPhoto(chatId: Long, fileId: String): ZIO[Any, ApiError, String] =
    for {
      _ <- ZIO.logDebug(s"Sending existing photo fileId=$fileId to chat $chatId")
      
      photoRequest = getSendPhotoRequest(chatId, fileId)
      response <- SttpClient.sendJson(client, photoRequest)
      telegramMessage <- TelegramApi.getTelegramResponse(response)
      fileId <- getPhotoId(telegramMessage, fileId)
    } yield fileId

  override def sendPhoto(chatId: Long, photo: TelegramFile): ZIO[Any, ApiError, String] =
    for {
      _ <- ZIO.logDebug(s"Sending telegram file ${photo.fileName} to chat $chatId")
      
      photoRequest = getSendPhotoRequest(chatId, photo)
      response <- SttpClient.sendJson(client, photoRequest)
      telegramMessage <- TelegramApi.getTelegramResponse(response)
      fileId <- getPhotoId(telegramMessage, photo.fileName)
    } yield fileId

  override def sendPhotos(chatId: Long, text: String, fileIds: List[String]): ZIO[Any, ApiError, Long] =
    for {
      _ <- ZIO.logDebug(s"Sending telegram files $fileIds to chat $chatId")

      mediaRequest = getSendMediaGroupRequest(chatId, text, fileIds)
      response <- SttpClient.sendJson(client, mediaRequest)
      telegramMessage <- TelegramApi.getTelegramResponse(response)
    } yield telegramMessage.messageId

  private def getPhotoId(response: TelegramMessageResponse, fileName: String) =
    ZIO.fromOption(response.photo.flatMap(_.lastOption.map(_.fileId)))
      .tapError(_ => ZIO.logError(s"No photo id found for file $fileName"))
      .orElseFail(ApiError.HttpCode(404, s"No photo id found for file $fileName"))

  private def getFileRequest(fileId: String) = {
    val uri = uri"$baseUrl/getFile?file_id=$fileId"
    basicRequest
      .get(uri)
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[TelegramFileResponse]])
  }

  private def getDownloadImageRequest(filePath: String) = {
    val uri = uri"$fileBaseUrl/$filePath"
    basicRequest
      .get(uri)
      .response(asByteArray)
  }

  private def getSendTextRequest(chatId: Long, text: String) = {
    val request = TelegramMessageRequest(chatId, text)
    SttpClient.postRequest(sendMessageUrl, request)
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[TelegramMessageResponse]])
  }

  private def getSendMarkupRequest(chatId: Long, text: String, markup: TelegramKeyboardMarkup) = {
    val request = TelegramMessageRequest(chatId, text, Some(markup))
    SttpClient.postRequest(sendMessageUrl, request)
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[TelegramMessageResponse]])
  }

  private def getSendPhotoRequest(chatId: Long, fileId: String) = {
    val request = TelegramPhotoRequest(chatId, fileId)
    SttpClient.postRequest(sendPhotoUrl, request)
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[TelegramMessageResponse]])
  }

  private def getSendPhotoRequest(chatId: Long, photo: TelegramFile) =
    basicRequest
      .post(sendPhotoUrl)
      .multipartBody(
        multipart("chat_id", chatId.toString),
        multipart("photo", photo.bytes)
          .fileName(photo.fileName)
          .contentType(MediaType.ImageJpeg)
      )
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[TelegramMessageResponse]])

  private def getSendMediaGroupRequest(chatId: Long, text: String, fileIds: List[String]) = {
    val mediaPhotos =
      fileIds.zipWithIndex.map { case (fileId, idx) =>
        if (idx == 0)
          TelegramInputMediaPhotoRequest(media = fileId, caption = Some(text))
        else
          TelegramInputMediaPhotoRequest(media = fileId)
      }
    val request = TelegramMediaGroupRequest(chatId, mediaPhotos)
    SttpClient.postRequest(sendPhotoUrl, request)
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[TelegramMessageResponse]])
  }