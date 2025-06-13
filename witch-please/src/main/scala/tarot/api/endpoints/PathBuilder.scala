package tarot.api.endpoints

import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.{HttpCodec, PathCodec}
import zio.http.endpoint.Endpoint

object PathBuilder {
  final val apiPath = "api"
}
