package tarot.infrastructure.services

import tarot.infrastructure.services.photo.{FileStorageService, PhotoService, TelegramFileService}

final case class TarotServiceLive(
  photoService: PhotoService,
  fileStorageService: FileStorageService,
  telegramFileService: TelegramFileService,
) extends TarotService 
