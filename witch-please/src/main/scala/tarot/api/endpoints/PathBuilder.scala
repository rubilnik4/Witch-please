package tarot.api.endpoints

import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.{HttpCodec, PathCodec}
import zio.http.endpoint.Endpoint

object PathBuilder {
  final val apiPath = "api"

  def getRoutePath(baseUrl: String, routePath: PathCodec[Unit]): URL = 
    val fullUrl = s"$baseUrl${routePath.encode}"
    URL.decode(fullUrl).toOption.get  
}
