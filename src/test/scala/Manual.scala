import io.github.binaryfoo.yatal.{Analyzer, Parser}

object Manual {

  def main(args: Array[String]) {
    val threads = Parser.parse("src/test/resources/examples/eg-1.txt")
    val groups = Analyzer.groupByStack(threads)
    for ((stack, threads) <- groups) {
      println(stack)
      println(threads)
    }
  }
}
