package x

import java.io.PrintWriter

class HtmlReport(writer: PrintWriter) {

  def start(): Unit = {
    writer.println(
      """
        |<html>
        |<head>
        |<link rel='stylesheet' href='bootstrap-3.3.6-dist/css/bootstrap.min.css'>
        |<link rel='stylesheet' href='bootstrap-3.3.6-dist/css/bootstrap-theme.min.css'>
        |<script src="jquery.min.js"></script>
        |<script src='bootstrap-3.3.6-dist/js/bootstrap.min.js'></script>
        |<script>
        |  function stackTraceFor() {
        |    console.log(this);
        |    console.log(this.getAttribute("thread-id"));
        |    var threadId = this.getAttribute("data-thread-id");
        |    return document.getElementById("thread-" + threadId).textContent;
        |  }
        |  $(function () {
        |      $('[data-toggle="popover"]').popover({
        |          content: stackTraceFor
        |      });
        |  })
        |</script>
        |<style>
        |.stackTrace {
        |  display: none;
        |}
        |.popover {
        |  font-size: smaller;
        |  max-width: 1200px;
        |}
        |</style>
        |</head>
        |<body>
      """.stripMargin)
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
    s"""<li>
        |<span data-container="body" data-toggle="popover" data-placement="bottom" data-thread-id="${thread.id}" tabindex="0" data-trigger="focus">
        |  ${thread.name} ${thread.stack.head} ${thread.state}
        |</span>
        |<span class="stackTrace" id="thread-${thread.id}">
        |${thread.stackTrace}
        |</span>
        |</li>
     """.stripMargin
  }
}

object HtmlReport {

  def apply(fileName: String): HtmlReport = {
    new HtmlReport(new PrintWriter(fileName))
  }
}
