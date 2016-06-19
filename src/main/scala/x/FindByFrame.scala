package x

import io.github.binaryfoo.yatal.{HtmlReport, Parser}

object FindByFrame {

  def main(args: Array[String]) {
    val frame = "org.h2"
    val threads = Parser.parse("src/test/resources/go-log-12.txt")
    val filtered = for (thread <- threads if thread.stack.exists(_.startsWith(frame))) yield thread

    val report = HtmlReport("byframe.html")
    report.start()
    report.printTree(s"Containing $frame (${filtered.size})", filtered)
    report.finish()
  }

}
