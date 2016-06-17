package jstack

object UrlParser {

  def parseQueryParams(search: String): Map[String, String] = {
    if (search.nonEmpty) {
      (for {
        kvPair <- search.substring(1).split('&')
        (key, value) <- parsePair(kvPair)
      } yield (key, value)).toMap
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
