package shared.infrastructure.services.storage

import shared.models.files.*
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import zio.{Cause, ZIO}

import java.util.UUID

final class S3FileStorageServiceLive(
  client: S3Client,
  bucket: String,
  keyPrefix: Option[String]
) extends FileStorageService:
  override def existFile(prefix: String, id: UUID): ZIO[Any, Throwable, Boolean] =
    val key = objectKey(prefix, id)
    for {
      _ <- ZIO.logDebug(s"Attempting to check file existence: $id in bucket=$bucket key=$key")

      exists <- ZIO.attemptBlocking(client.headObject(headObjectRequest(key))).as(true)
        .catchSome {
          case _: NoSuchKeyException => ZIO.succeed(false)
          case ex: S3Exception if ex.statusCode() == 404 => ZIO.succeed(false)
        }
        .tapError(ex => ZIO.logErrorCause(s"Failed to check file existence for s3://$bucket/$key", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Failed to check file existence for s3://$bucket/$key", ex))
    } yield exists

  override def getFile(prefix: String, id: UUID): ZIO[Any, Throwable, FileBytes] =
    val key = objectKey(prefix, id)
    for {
      _ <- ZIO.logDebug(s"Attempting to get file: $id from bucket=$bucket key=$key")
      
      response <- ZIO.attemptBlocking(client.getObjectAsBytes(getObjectRequest(key)))
        .catchSome {
          case _: NoSuchKeyException =>
            ZIO.fail(new RuntimeException(s"File not found at s3://$bucket/$key"))
          case ex: S3Exception if ex.statusCode() == 404 =>
            ZIO.fail(new RuntimeException(s"File not found at s3://$bucket/$key", ex))
        }
        .tapError(ex => ZIO.logErrorCause(s"Failed to read file s3://$bucket/$key", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Failed to read file s3://$bucket/$key", ex))
      fileName = key.split("/").lastOption.getOrElse(id.toString)
    } yield FileBytes(fileName, response.asByteArray())

  override def storeFile(prefix: String, fileBytes: FileBytes): ZIO[Any, Throwable, FileStored] =
    val id = UUID.randomUUID()
    val key = objectKey(prefix, id)
    for {
      _ <- ZIO.logDebug(s"Attempting to store file: ${fileBytes.fileName} in bucket=$bucket key=$key")
      
      _ <- ZIO.attemptBlocking(client.putObject(putObjectRequest(key), RequestBody.fromBytes(fileBytes.bytes)))
        .tapError(ex => ZIO.logErrorCause(s"Failed to upload file ${fileBytes.fileName} to s3://$bucket/$key", Cause.fail(ex)))
        .mapError(ex => new RuntimeException(s"Failed to upload file ${fileBytes.fileName} to s3://$bucket/$key", ex))
    } yield FileStored.S3(id, bucket, key)

  override def deleteFile(prefix: String, id: UUID): ZIO[Any, Throwable, Boolean] =
    val key = objectKey(prefix, id)
    for {
      _ <- ZIO.logDebug(s"Attempting to delete file: $id from bucket=$bucket key=$key")
      
      exists <- existFile(prefix, id)
      _ <- ZIO.when(exists) {
        ZIO.attemptBlocking(client.deleteObject(deleteObjectRequest(key)))
          .tapError(ex => ZIO.logErrorCause(s"Failed to delete file s3://$bucket/$key", Cause.fail(ex)))
          .mapError(ex => new RuntimeException(s"Failed to delete file s3://$bucket/$key", ex))
          .unit
      }
    } yield exists

  private def objectKey(prefix: String, id: UUID): String =
    fullPrefix(prefix)
      .map(_.trim)
      .filter(_.nonEmpty)
      .map(prefix => s"${prefix.stripSuffix("/")}/$id")
      .getOrElse(id.toString)

  private def fullPrefix(prefix: String): Option[String] =
    List(keyPrefix, Option(prefix))
      .flatten
      .map(_.trim)
      .filter(_.nonEmpty)
      .reduceOption((left, right) => s"${left.stripSuffix("/")}/${right.stripPrefix("/")}")

  private def putObjectRequest(key: String): PutObjectRequest =
    PutObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .contentType("application/octet-stream")
      .build()

  private def headObjectRequest(key: String): HeadObjectRequest =
    HeadObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .build()

  private def deleteObjectRequest(key: String): DeleteObjectRequest =
    DeleteObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .build()

  private def getObjectRequest(key: String): GetObjectRequest =
    GetObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .build()
