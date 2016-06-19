import java.io.FileWriter

import io.github.binaryfoo.yatal.{Analyzer, InMemoryHtmlReport, Parser}
import scalatags.Text.all._

object Manual {

  def main(args: Array[String]) {
    val threads = Parser.parse("src/test/resources/go-log-12.txt")
    val groups = Analyzer.groupByStack(threads)
    val report = new InMemoryHtmlReport()
    val trs = report.groupToTableRow(groups.head._2)

    val a = new FileWriter("a.html")
    a.write(
      html(
        body(
          table(trs)
        )).render
    )
    a.close()
  }
}
