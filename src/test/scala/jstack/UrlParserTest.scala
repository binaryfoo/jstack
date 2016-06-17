package jstack

import org.scalatest.{FlatSpec, Matchers}

class UrlParserTest extends FlatSpec with Matchers {

  "Parser" should "extract query params" in {
    val params = UrlParser.parseQueryParams("?url=http://example.com/file%20name.txt")
    params shouldBe Map("url" -> "http://example.com/file name.txt")
  }
}
