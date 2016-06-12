package x

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

case class Thread(id: String, name: String, state: String, stack: Seq[String] = Seq.empty)

object Parser {
  val ThreadDetails = """(\d+), ([^,]+), (.+)""".r

  def parse(fileName: String): Seq[Thread] = {
    var inStackTrace = false
    var thread: Thread = null
    var threads = ArrayBuffer[Thread]()

    for (line <- Source.fromFile(fileName).getLines().dropWhile(line => !line.startsWith("All thread stacktraces"))) {
      line match {
        case ThreadDetails(id, name, state) =>
          if (thread != null)
            threads += thread
          thread = Thread(id, name, state)
        case "Stacktrace:" =>
          inStackTrace = true
        case "" =>
          inStackTrace = false
        case frame if inStackTrace =>
          thread = thread.copy(stack = thread.stack :+ frame)
        case _ =>
      }
    }
    threads
  }
}
