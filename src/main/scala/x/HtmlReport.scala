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
        |    var threadId = this.getAttribute("data-thread-id");
        |    return document.getElementById("thread-" + threadId).innerHTML;
        |  }
        |  $(function () {
        |      $('[data-toggle="popover"]').popover({
        |          content: stackTraceFor,
        |          html: true
        |      });
        |  })
        |</script>
        |<style>
        |span.stackTrace {
        |  display: none;
        |}
        |.popover {
        |  font-size: smaller;
        |  max-width: 1200px;
        |  white-space: pre-wrap;
        |}
        |.package {
        |  font-size: smaller;
        |  color: dimgray;
        |  text-align: right;
        |}
        |div.methodCall {
        |  display: inline-block;
        |}
        |span.sourceReference {
        |  font-size: smaller;
        |  color: dimgray;
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
        |<span data-container="body" data-toggle="popover" data-placement="bottom" data-thread-id="${thread.id}" tabindex="0">
        |  ${thread.name} ${decorateTopFrame(thread.stack.head)} ${thread.state}
        |</span>
        |<span class="stackTrace" id="thread-${thread.id}">
        |<table class="stackTrace">
        |${decorateFrames(thread.stack)}
        |</table>
        |</span>
        |</li>
     """.stripMargin
  }

  val StackFrame = """([^(]+)"""

  def decorateFrames(frames: Seq[String]): String = {
    frames.map(decorateFrame).mkString("\n")
  }

  def decorateTopFrame(frame: String): String = {
    val (pkg, methodName, sourceReference) = splitMethodCall(frame)
    s"""<span class="package">$pkg</span><div class="methodCall">.$methodName(<span class="sourceReference">$sourceReference</span>)</div>"""
  }

  def decorateFrame(frame: String): String = {
    val (pkg, methodName, sourceReference) = splitMethodCall(frame)
    s"""<tr>
       |<td class="package">$pkg</td><td class="methodCall">.$methodName(<span class="sourceReference">$sourceReference</span>)</td>
       |</tr>""".stripMargin
  }

  val MethodCallParts = """([^(]+)\((.+)\)""".r

  def splitMethodCall(frame: String): (String, String, String) = {
    val parts = frame.split('.')
    val methodCallIndex = parts.indexWhere(_.contains("("))
    val (pkg, methodCall) = parts.splitAt(methodCallIndex)
    val MethodCallParts(methodName, sourceReference) = methodCall.mkString(".")
    (cleanJRubyPackage(pkg.mkString(".")), methodName, cleanJRubySourceReference(sourceReference))
  }

  def cleanJRubyPackage(pkg: String): String = {
    pkg.replaceAll("""_[0-9a-f]{10,}""", "")
  }

  def cleanJRubySourceReference(ref: String): String = {
    if (ref.length > 30) {
      ref.replaceFirst(".*WEB-INF/", "").replaceAll("""_[0-9a-f]{10,}""", "")
    } else {
      ref
    }
  }
}

object HtmlReport {

  def apply(fileName: String): HtmlReport = {
    new HtmlReport(new PrintWriter(fileName))
  }
}
