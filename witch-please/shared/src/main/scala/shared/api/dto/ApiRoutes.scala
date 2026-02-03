package shared.api.dto

import zio.http.URL

import java.net.URLEncoder

object ApiRoutes {
  def make(baseUrl: String, segments: String*): URL =
    make(baseUrl, segments, Map.empty)

  def make(baseUrl: String, segments: Seq[String], queryParams: Map[String, String]): URL = {
    val base = baseUrl.stripSuffix("/")
    val path = segments.map(_.stripPrefix("/")).mkString("/")

    val query =
      if (queryParams.isEmpty) ""
      else
        queryParams
          .map { case (k, v) => s"${encode(k)}=${encode(v)}" }
          .mkString("?", "&", "")

    URL.decode(s"$base/$path$query").getOrElse {
      throw new IllegalArgumentException(s"Invalid URL: $base/$path$query")
    }
  }

  private def encode(value: String): String =
    URLEncoder.encode(value, "UTF-8")
}
