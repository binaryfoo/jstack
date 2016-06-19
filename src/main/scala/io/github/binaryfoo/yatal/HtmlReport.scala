package io.github.binaryfoo.yatal

import java.io.{PrintWriter, StringWriter}

import scalatags.Text
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

  def printBlockingTree(roots: Seq[DeDuplicatedBlockingTree]): Unit = {
    writer.println(ul(
      for (root <- roots) yield toListElement(root)
    ))
  }

  def toListElement(root: DeDuplicatedBlockingTree): Text.TypedTag[String] = {
    val (expandLink, collapsedTable) = formatExpandableStack(root.lockHolder.stack, threadSummary(root.lockHolder))
    li(
      expandLink,
      s" (${root.totalChildren} downstream) ",
      collapsedTable,
      ul(
        for (group <- root.blocked) yield {
          val (expandLink, collapsedTable) = formatExpandableStack(group.stack, threadSummary(group.threads.head))
          li(
            expandLink,
            if (group.threads.size > 1) {
              val more = group.threads.drop(1)
              threadListPopover(s" + ${more.size} duplicates ", more.map(_.name))
            } else {
              " "
            },
            collapsedTable
          )
        },
        for (internal <- root.children) yield toListElement(internal)
      )
    )
  }

  def threadSummary(thread: Thread): Seq[Modifier] = {
    Seq(
      thread.name,
      " ",
      decorateTopFrame(thread.stack.headOption),
      " ",
      thread.state
    )
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
    val (expandLink, collapsedTable) = formatExpandableStack(first.stack, decorateTopFrame(first.stack.headOption))
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

  def printThreads(threads: Seq[Thread]): Unit = {
    writer.print(table(
      cls := "table",
      thead(
        th("Name"),
        th("Frame"),
        th("State")
      ),
      tbody(
        for (thread <- threads) yield threadToTableRow(thread)
      )
    ))
  }

  def threadToTableRow(thread: Thread) = {
    val (expandLink, collapsedTable) = formatExpandableStack(thread.stack, decorateTopFrame(thread.stack.headOption))
    Seq(
      tr(
        td(thread.name),
        td(expandLink),
        td(cls := "threadState", thread.state)
      ),
      tr(
        td(colspan := 3,
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
        for (frame <- stack if frame.nonEmpty) yield decorateFrame(frame)
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

  private def decorateTopFrame(maybeFrame: Option[String]) = {
    maybeFrame match {
      case Some(frame) if frame.nonEmpty =>
        val (pkg, methodName, sourceReference) = splitMethodCall(frame)
        Seq(
          span(cls := "package", pkg),
          div(cls := "methodCall",
            s".$methodName(",
            span(cls := "sourceReference", sourceReference),
            ")"
          )
        )
      case _ =>
        Seq.empty
    }
  }

  private def decorateFrame(frame: String) = {
    val (pkg, methodName, sourceReference) = splitMethodCall(frame)
    tr(
      td(cls := "package", pkg),
      td(cls := "methodCall",
        s".$methodName(",
        span(cls := "sourceReference", sourceReference),
        ")"
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
