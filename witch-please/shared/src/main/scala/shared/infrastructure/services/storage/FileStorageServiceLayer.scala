package shared.infrastructure.services.storage

import shared.application.configurations.S3StorageConfig
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import zio.nio.file.Path
import zio.{Scope, ZIO, ZLayer}

import java.net.URI

object FileStorageServiceLayer {
  val localLive: ZLayer[String, Throwable, FileStorageService] =
    ZLayer.fromZIO {
      for {
        localPath <- ZIO.service[String]
        service <- makeLocal(localPath)
      } yield service
    }
    
  val s3Live: ZLayer[S3StorageConfig, Throwable, FileStorageService] =
    ZLayer.scoped {
      for {
        config <- ZIO.service[S3StorageConfig]
        service <- makeS3(config)
      } yield service
    }

  private def makeLocal(rawPath: String): ZIO[Any, Throwable, FileStorageService] =
    for {
      path <- ZIO.attempt(Path(rawPath))
        .mapError(ex => new RuntimeException(s"Invalid path '$rawPath': ${ex.getMessage}", ex))
    } yield new LocalFileStorageServiceLive(path)

  private def makeS3(config: S3StorageConfig): ZIO[Scope, Throwable, FileStorageService] =
    ZIO.acquireRelease(
        ZIO.attempt {
          S3Client.builder()
            .endpointOverride(URI.create(config.endpoint))
            .region(Region.of(config.region))
            .credentialsProvider(
              StaticCredentialsProvider.create(
                AwsBasicCredentials.create(config.accessKeyId, config.secretAccessKey)
              )
            )
            .forcePathStyle(false)
            .build()
        }.mapError(ex => new RuntimeException(s"Failed to create S3 client for endpoint '${config.endpoint}'", ex))
      )(client => ZIO.attempt(client.close()).ignoreLogged)
      .map(client => new S3FileStorageServiceLive(client, config.bucket, config.keyPrefix))  
}
