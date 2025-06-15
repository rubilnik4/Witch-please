package tarot.application.handlers

import tarot.application.commands.SpreadCommand
import tarot.domain.models.TarotError
import tarot.layers.AppEnv
import zio.ZIO

import java.time.Instant

final class SpreadCommandHandlerLive extends SpreadCommandHandler {
  override def handle(command: SpreadCommand): ZIO[AppEnv, TarotError, String] = {
    for {
      _ <- ZIO.logInfo(s"Executing spread command for ${command.spread.id}")

      priceA <- getPrice(assetSpreadId.assetA)
      priceB <- getPrice(assetSpreadId.assetB)

      spread = getSpread(priceA, priceB)
      _ <- saveSpread(spread)

      newState <- updateState(cmd.spreadState, spread)
    } yield SpreadResult(spread, newState)
  }

  private def getPrice(assetId: AssetId): ZIO[AppEnv, MarketError, Price] =
    for {
      marketData <- ZIO.serviceWith[AppEnv](_.marketData)
      _ <- ZIO.logInfo(s"Fetching price for $assetId")
      result <- marketData
        .getPrice(assetId)
        .catchAll(_ => marketData.getLastPrice(assetId))
        .tapBoth(
          _ => ZIO.logError(s"No price at all for $assetId"),
          _ => ZIO.logInfo(s"Using last price for $assetId"))
    } yield result

  private def getSpread(priceA: Price, priceB: Price): Spread = {
    val spreadValue = (priceA.value - priceB.value).abs
    Spread(priceA, priceB, spreadValue, Instant.now())
  }

  private def removeSpreadCache(spread: Spread): ZIO[AppEnv, MarketError, Unit] =
    for {
      cache <- ZIO.serviceWith[AppEnv](_.marketCache)
      _ <- cache.priceCache.invalidate(spread.priceA.assetId)
      _ <- cache.priceCache.invalidate(spread.priceB.assetId)
      _ <- cache.spreadCache.invalidate(Spread.toAssetSpread(spread))
    } yield ()

  private def saveSpread(spread: Spread): ZIO[AppEnv, MarketError, SpreadId] =
    for {
      marketRepository <- ZIO.serviceWith[AppEnv](_.marketRepository)
      spreadId <- marketRepository.saveSpread(spread)
      _ <- removeSpreadCache(spread)
      _ <- ZIO.logInfo(s"Spread saved with id: $spreadId")
    } yield spreadId

  private def updateState(state: SpreadState, spread: Spread): ZIO[AppEnv, MarketError, SpreadState] =
    for {
      appConfig <- ZIO.serviceWith[AppEnv](_.appConfig.project)
      _ <- ZIO.when(spread.value > appConfig.spreadThreshold) {
        ZIO.logInfo(s"Threshold ${appConfig.spreadThreshold} exceeded for spread $spread")
      }

      updatedHistory = (spread.value :: state.spreadHistory).take(appConfig.maxHistorySize)
      newState = state.copy(
        lastSpread = Some(spread.value),
        spreadHistory = updatedHistory,
        isThresholdExceeded = spread.value > appConfig.spreadThreshold
      )
    } yield newState
}
