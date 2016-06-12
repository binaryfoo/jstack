import x.Parser

object Main {

  def main(args: Array[String]) {
    val threads = Parser.parse("src/test/resources/gosupport2.txt")

    threads
      .filter(_.stack.exists(_.contains("org.h2")))
      .groupBy(_.state)
      .mapValues(_.size)
      .foreach(println)
  }
}
