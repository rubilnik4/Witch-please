package tarot.api.endpoints

import zio.http.*
import zio.http.codec.PathCodec

object PathBuilder {
  final val apiPath = "api"

  def getRoutePath(baseUrl: String, routePath: PathCodec[Unit]): URL = 
    val fullUrl = s"$baseUrl${routePath.toString}"
    URL.decode(fullUrl).toOption.get  
}
