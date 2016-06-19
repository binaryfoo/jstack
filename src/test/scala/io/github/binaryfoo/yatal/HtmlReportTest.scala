package io.github.binaryfoo.yatal

import org.scalatest.{FlatSpec, Matchers}

class HtmlReportTest extends FlatSpec with Matchers {

  "Report" should "produce state table" in {
    val report = new InMemoryHtmlReport()
    report.printStateSummary(Map("RUNNABLE" -> 3, "BLOCKED" -> 2))
    report.render shouldBe """<table class="table"><thead><tr><td>State</td><td>Count</td></tr></thead><tbody><tr><td>RUNNABLE</td><td>3</td></tr><tr><td>BLOCKED</td><td>2</td></tr></tbody></table>"""
  }
}
