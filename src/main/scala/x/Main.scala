package x

import io.github.binaryfoo.yatal.{BlockingTree, HtmlReport, Parser, Thread}

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

    val roots = BlockingTree.buildBlockingTree(threads)

    val report = HtmlReport("report.html")
    report.start()
    for (root <- roots) {
      report.printTree(root)
    }

    report.finish()
  }

  def countByState(threads: Seq[Thread]): Map[String, Int] = threads.groupBy(_.state).mapValues(_.size)

}
