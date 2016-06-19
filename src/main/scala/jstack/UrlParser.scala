package jstack

import scala.scalajs.js

object UrlParser {

  def parseQueryParams(search: String): Map[String, String] = {
    if (search.nonEmpty) {
      (for {
        kvPair <- search.substring(1).split('&')
        (key, value) <- parsePair(kvPair)
      } yield (key, js.URIUtils.decodeURIComponent(value))).toMap
    } else {
      Map.empty
    }
  }

  private def parsePair(kvPair: String): Option[(String, String)] = {
    kvPair.split('=') match {
      case Array(key, value) => Some(key, value)
      case _ => None
    }
  }

}
