package io.github.binaryfoo.yatal

object Analyzer {

  def groupByStack(threads: Seq[Thread]): Seq[(Seq[String], Seq[Thread])] = {
    threads.groupBy(_.stack).toSeq.sortBy(_._2.size).reverse
  }

  def groupByState(threads: Seq[Thread]): Map[String, Int] = threads.groupBy(_.state).mapValues(_.size)
}
