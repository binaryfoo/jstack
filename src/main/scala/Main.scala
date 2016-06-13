import x.{Parser, Thread}

object Main {

  def main(args: Array[String]) {
    val allThreads = Parser.parse("src/test/resources/gosupport2.txt")

    countByState(allThreads).foreach(println)

    // dbcp threads aren't blocked ...
    val edges = for {
      thread <- allThreads.toSet if thread.state == "BLOCKED"
      lockHolder <- allThreads.find(_.hasLockFromFrame(thread.stack.head))
    } yield (thread, lockHolder)

    val roots = for ((_, holder) <- edges if !edges.exists(_._1 == holder)) yield holder

    for (root <- roots) {
      printTree(root, edges)
    }

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
    println(s"$indent${root.name} ${root.stack.head} ${root.state}")
    val nextIndent = indent + "  "
    for ((src, dest) <- edges if dest == root) {
      printTree(src, edges, nextIndent)
    }
  }
}
