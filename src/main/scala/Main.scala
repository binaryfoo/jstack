import x.{HtmlReport, Parser, Thread}

object Main {

  def main(args: Array[String]) {
    val threads = Parser.parse("src/test/resources/gosupport2.txt")

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

  def printBasic(threads: Seq[Thread]): Unit = {
    for ((stack, threads) <- threads.groupBy(_.stack).toSeq.sortBy(_._2.size).reverse) {
      val start = stack.take(2).mkString(", ")
      val states = threads.map(_.state).toSet.mkString("{", ", ", "}")
      println(s"${threads.size}\t$states\t$start")
    }
  }

  def printTree(root: Thread, edges: Set[(Thread, Thread)], indent: String = ""): Unit = {
    println(formatThread(root, indent))
    val nextIndent = indent + "  "
    for ((src, dest) <- edges if dest == root) {
      printTree(src, edges, nextIndent)
    }
  }

  def formatThread(thread: Thread, indent: String = ""): String = {
    s"$indent${thread.name} ${thread.stack.head} ${thread.state}"
  }
}
