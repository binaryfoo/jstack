package io.github.binaryfoo.yatal

import java.io.PrintWriter

class HtmlReport(writer: PrintWriter) {

  private var stackId = 1

  def start(): Unit = {
    writer.println(
      """
        |<html>
        |<head>
        |<link rel='stylesheet' href='bower_components/bootstrap/dist/css/bootstrap.min.css'>
        |<link rel='stylesheet' href='bower_components/bootstrap/dist/css/bootstrap-theme.min.css'>
        |<script src="bower_components/jquery/dist/jquery.min.js"></script>
        |<script src='bower_components/bootstrap/dist/js/bootstrap.min.js'></script>
        |<script>
        |  function stackTraceFor() {
        |    var stackId = this.getAttribute("data-stack-id");
        |    return document.getElementById(stackId).innerHTML;
        |  }
        |  $(function () {
        |      $('[data-toggle="popover"]').popover({
        |          content: stackTraceFor,
        |          html: true
        |      });
        |  })
        |</script>
        |<style>
        |.stackTrace,.popover {
        |  font-size: smaller;
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

  def printTree(root: DeDuplicatedBlockingTree): Unit = {
    writer.println("<ul>")
    writer.println(formatThread(root.lockHolder, s"(${root.totalChildren} downstream)"))
    for (group <- root.blocked) {
      writer.println("<ul>")
      val first = summariseThread(group.threads.head)
      val comment = if (group.threads.size > 1) {
        val more = group.threads.drop(1)
        threadListPopover(s"+ ${more.size} more with same stack", more.map(_.name))
      } else {
        ""
      }
      writer.println(formatStack(group.stack, first, comment))
      writer.println("</ul>")
    }
    for (internal <- root.children) {
      printTree(internal)
    }
    writer.println("</ul>")
  }

  def printTree(root: Thread, edges: Set[(Thread, Thread)] = Set.empty): Unit = {
    val children = edges.filter(_._2 == root)
    val comment = if (children.nonEmpty) "(" + children.size + " downstream)" else ""
    writer.println("<ul>")
    writer.println(formatThread(root, comment))
    for ((src, dest) <- children) {
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

  def formatThread(thread: Thread, comment: String = ""): String = {
    formatStack(thread.stack, summariseThread(thread), comment)
  }

  def summariseThread(thread: Thread): String = {
    s"${thread.name} ${decorateTopFrame(thread.stack.head)} ${thread.state}"
  }

  def formatStack(stack: Seq[String], description: String, comment: String = ""): String = {
    val id = s"stack-$stackId"
    stackId += 1
    s"""<li>
        |<a role="button" href="#$id" data-toggle="collapse" aria-expanded="false" aria-controls="$id" class="stack">
        |  $description
        |</a>
        | $comment
        |<span class="stackTrace collapse" id="$id">
        |<table class="stackTrace">
        |${decorateFrames(stack)}
        |</table>
        |</span>
        |</li>
     """.stripMargin
  }

  def threadListPopover(text: String, names: Seq[String]): String = {
    val popoverText = names.mkString("<br>")
    s"""<span data-container="body" data-toggle="popover" data-placement="bottom" tabindex="0" class="comment" data-content="$popoverText">$text</span>"""
  }

  val StackFrame = """([^(]+)"""

  def decorateFrames(frames: Seq[String]): String = {
    frames.map(decorateFrame).mkString("\n")
  }

  def decorateTopFrame(frame: String): String = {
    if (frame.nonEmpty) {
      val (pkg, methodName, sourceReference) = splitMethodCall(frame)
      s"""<span class="package">$pkg</span><div class="methodCall">.$methodName(<span class="sourceReference">$sourceReference</span>)</div>"""
    } else {
      ""
    }
  }

  def decorateFrame(frame: String): String = {
    if (frame.nonEmpty) {
      val (pkg, methodName, sourceReference) = splitMethodCall(frame)
      s"""<tr>
          |<td class="package">$pkg</td><td class="methodCall">.$methodName(<span class="sourceReference">$sourceReference</span>)</td>
          |</tr>""".stripMargin
    } else {
      ""
    }
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
