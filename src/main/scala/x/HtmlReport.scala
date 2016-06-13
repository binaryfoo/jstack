package x

import java.io.PrintWriter

class HtmlReport(writer: PrintWriter) {

  def start(): Unit = {
    writer.println("<html>")
    writer.println("<body>")
  }

  def finish(): Unit = {
    writer.println("</body>")
    writer.println("</html>")
    writer.close()
  }

  def printTree(root: Thread, edges: Set[(Thread, Thread)]): Unit = {
    writer.println("<ul>")
    writer.println(formatThread(root))
    for ((src, dest) <- edges if dest == root) {
      printTree(src, edges)
    }
    writer.println("</ul>")
  }

  def printTree(root: String, children: Seq[Thread]): Unit = {
    writer.println("<ul>")
    writer.println("<li>" + root + "</li>")
    writer.println("<ul>")
    for (child <- children) {
      writer.println(formatThread(child) )
    }
    writer.println("</ul>")
    writer.println("</ul>")
  }

  def formatThread(thread: Thread): String = {
    s"<li>${thread.name} ${thread.stack.head} ${thread.state}</li>"
  }
}

object HtmlReport {

  def apply(fileName: String): HtmlReport = {
    new HtmlReport(new PrintWriter(fileName))
  }
}
