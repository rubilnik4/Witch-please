package tarot.integration

import shared.infrastructure.services.storage.StoragePrefix
import shared.models.tarot.authorize.ClientType
import tarot.domain.models.photo.PhotoId
import tarot.domain.models.spreads.SpreadId
import tarot.fixtures.TarotTestFixtures
import tarot.layers.{TarotEnv, TestTarotEnvLayer}
import tarot.states.TestPhotoDedupState
import zio.*
import zio.test.*
import zio.test.TestAspect.sequential

import java.util.UUID

object PhotoDedupIntegrationSpec extends ZIOSpecDefault {
  private final val clientId = "123456789"
  private final val channelId = 12345
  private final val clientType = ClientType.Telegram
  private final val clientSecret = "test-secret-token"
  private final val cardsCount = 1

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Photo dedup integration")(
    test("should create two spread photos and one photo object for identical photo") {
      for {
        photoSourceId <- TarotTestFixtures.createPhoto(channelId)
        userId <- TarotTestFixtures.createUser(clientId, clientType, clientSecret)
        spreadId1 <- TarotTestFixtures.createSpread(userId, cardsCount, photoSourceId)
        spreadId2 <- TarotTestFixtures.createSpread(userId, cardsCount, photoSourceId)
        spreadQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.spreadQueryHandler)
        photoQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)
        spread1 <- spreadQueryHandler.getSpread(spreadId1)
        spread2 <- spreadQueryHandler.getSpread(spreadId2)
        photo1 <- photoQueryHandler.getPhoto(spread1.photo.id)
        photo2 <- photoQueryHandler.getPhoto(spread2.photo.id)

        ref <- ZIO.service[Ref.Synchronized[TestPhotoDedupState]]
        state = TestPhotoDedupState(
          spreadIds = List(spreadId1.id, spreadId2.id),
          photoIds = List(photo1.id.id, photo2.id.id),
          fileId = Some(photo1.photoObject.fileId)
        )
        _ <- ref.set(state)
      } yield assertTrue(
        photo1.id != photo2.id,
        photo1.photoObject == photo2.photoObject
      )
    },

    test("should delete both spreads and remove photo records and stored file") {
      for {
        ref <- ZIO.service[Ref.Synchronized[TestPhotoDedupState]]
        state <- ref.get
        spreadIds <- ZIO.fromOption(state.spreadIds match {
          case spreadId1 :: spreadId2 :: Nil => Some((spreadId1, spreadId2))
          case _ => None
        }).orElseFail(new RuntimeException("spreadIds not set"))
        photoIds <- ZIO.fromOption(state.photoIds match {
          case photoId1 :: photoId2 :: Nil => Some((PhotoId(photoId1), PhotoId(photoId2)))
          case _ => None
        }).orElseFail(new RuntimeException("photoIds not set"))
        fileId <- ZIO.fromOption(state.fileId).orElseFail(new RuntimeException("fileId not set"))

        spreadCommandHandler <- ZIO.serviceWith[TarotEnv](_.commandHandlers.spreadCommandHandler)
        photoQueryHandler <- ZIO.serviceWith[TarotEnv](_.queryHandlers.photoQueryHandler)
        _ <- spreadCommandHandler.deleteSpread(SpreadId(spreadIds._1))
        _ <- spreadCommandHandler.deleteSpread(SpreadId(spreadIds._2))

        firstPhotoExists <- photoQueryHandler.existPhoto(photoIds._1)
        secondPhotoExists <- photoQueryHandler.existPhoto(photoIds._2)
        fileStorageService <- ZIO.serviceWith[TarotEnv](_.services.fileStorageService)
        fileExists <- fileStorageService.existFile(StoragePrefix.photo, fileId)
      } yield assertTrue(
        !firstPhotoExists,
        !secondPhotoExists,
        !fileExists
      )
    }
  ).provideShared(
    TestTarotEnvLayer.testEnvLive,
    testPhotoDedupStateLayer
  ) @@ sequential
  
  private val testPhotoDedupStateLayer: ZLayer[Any, Nothing, Ref.Synchronized[TestPhotoDedupState]] =
    ZLayer.fromZIO(Ref.Synchronized.make(TestPhotoDedupState()))
}
