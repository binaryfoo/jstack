import x.{HtmlReport, Parser}

object FindOverlaps {

  def main(args: Array[String]) {

    val threads = Parser.parse("src/test/resources/go-log-12.txt")

    val report = HtmlReport("duplicates.html")
    report.start()

    for ((stack, threads) <- threads.groupBy(_.stack).toSeq.sortBy(_._2.size).reverse) {
      val states = threads.map(_.state).toSet.mkString("{", ",", "}")
      report.printTree(threads.size + " " + states, threads)
    }

    report.finish()
  }

}
