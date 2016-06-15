import x.{HtmlReport, Parser, Thread}

object Main {

  def main(args: Array[String]) {
    if (args.length != 1) {
      System.err.println("usage: <output-of-http://go.example.com/go/api/support>")
      System.exit(1)
    }

    val threads = Parser.parse(args(0))

    for ((state, count) <- countByState(threads)) {
      println(s"$state\t$count")
    }
    println(s"${threads.size} total")

    // dbcp threads aren't blocked ...
    val edges = for {
      thread <- threads.toSet if thread.state == "BLOCKED"
      lockHolder <- threads.find(_.hasLockFromFrame(thread.stack.head))
    } yield (thread, lockHolder)

    val roots = for ((_, holder) <- edges if !edges.exists(_._1 == holder)) yield holder

    val report = HtmlReport("report.html")
    report.start()
    for (root <- roots) {
      report.printTree(root, edges)
    }

    // dpcp threads
    val waitingForConnection = threads.filter(_.waitingFor("PoolingDataSource.getConnection"))
    report.printTree("Waiting for PoolingDataSource.getConnection", waitingForConnection)

    report.finish()
  }

  def countByState(threads: Seq[Thread]): Map[String, Int] = threads.groupBy(_.state).mapValues(_.size)

}
