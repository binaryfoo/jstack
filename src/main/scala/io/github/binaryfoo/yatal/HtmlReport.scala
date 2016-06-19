package io.github.binaryfoo.yatal

import java.io.{PrintWriter, StringWriter}

import scalatags.Text.all._

class HtmlReport(val writer: PrintWriter) {

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
        threadListPopover(s"+ ${more.size} more with same stack", more.map(_.name)).render
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

  def printGroupedByStack(groups: Seq[Seq[Thread]]): Unit = {
    writer.print(table(
      cls := "table",
      thead(
        th("Name"),
        th("Frame"),
        th("State"),
        th("Count")
      ),
      tbody(
        for (group <- groups) yield groupToTableRow(group)
      )
    ))
  }

  def groupToTableRow(threads: Seq[Thread]) = {
    val first = threads.head
    val (expandLink, collapsedTable) = formatExpandableStack(first.stack, _decorateTopFrame(first.stack.head))
    Seq(
      tr(
        td(first.name),
        td(expandLink),
        td(cls := "threadState", first.state),
        td(cls := "text-right", if (threads.size > 1) {
          threadListPopover(threads.size.toString, threads.map(_.name))
        } else {
          threads.size.toString
        })
      ),
      tr(
        td(colspan := 4,
          collapsedTable)
      )
    )
  }

  def printHeading(text: String): Unit = {
    writer.print(h4(text))
  }

  def printStateSummary(states: Map[String, Int]): Unit = {
    val rows = for ((state, count) <- states) yield tr(td(state), td(count))
    writer.print(table(
      cls := "table",
      thead(tr(td("State"), td("Count"))),
      tbody(rows.toSeq)
    ))
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

  private def formatExpandableStack(stack: Seq[String], description: Seq[Modifier]) = {
    val idAttr = s"stack-$stackId"
    stackId += 1
    val link = a(
      role := "button",
      href := s"#$idAttr",
      data("toggle") := "collapse",
      aria.expanded := "false",
      aria.controls := s"$idAttr",
      cls := "stack",
      description
    )
    val collapsedTable = div(
      cls := "stackTrace collapse",
      id := idAttr,
      table(cls := "stackTrace",
        for (frame <- stack if frame.nonEmpty) yield _decorateFrame(frame)
      )
    )
    (link, collapsedTable)
  }

  def threadListPopover(text: String, names: Seq[String]) = {
    val popoverText = names.mkString("<br>")
    span(
      data("container") := "body",
      data("toggle") := "popover",
      data("placement") := "bottom",
      data("content") := popoverText,
      tabindex := 0,
      cls := "comment",
      text
    )
  }

  val StackFrame = """([^(]+)"""

  def decorateFrames(frames: Seq[String]): String = {
    frames.map(decorateFrame).mkString("\n")
  }

  def decorateTopFrame(frame: String): String = {
    _decorateTopFrame(frame).map(_.render).mkString("\n")
  }

  def decorateFrame(frame: String): String = {
    if (frame.nonEmpty) {
      _decorateFrame(frame).render
    } else {
      ""
    }
  }

  private def _decorateTopFrame(frame: String) = {
    if (frame.isEmpty) {
      Seq.empty
    } else {
      val (pkg, methodName, sourceReference) = splitMethodCall(frame)
      Seq(
        span(cls := "package", pkg),
        div(cls := "methodCall",
          s".$methodName(",
          span(cls := "sourceReference", sourceReference),
          ")"
        )
      )
    }
  }

  private def _decorateFrame(frame: String) = {
    val (pkg, methodName, sourceReference) = splitMethodCall(frame)
    tr(
      td(cls := "package", pkg),
      td(cls := "methodCall",
        s".$methodName(",
        span(cls := "sourceReference", sourceReference)
      )
    )
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

class InMemoryHtmlReport(val buffer: StringWriter = new StringWriter()) extends HtmlReport(new PrintWriter(buffer)) {
  def render: String = {
    writer.flush()
    buffer.toString
  }
}

object HtmlReport {

  def apply(fileName: String): HtmlReport = {
    new HtmlReport(new PrintWriter(fileName))
  }
}
