package jstack

import java.io.{PrintWriter, StringWriter}

import io.github.binaryfoo.yatal._
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global

case class JstackController(model: JstackModel, output: dom.Element, input: dom.html.TextArea, contains: dom.html.TextArea, viewType: dom.html.Input) {

  def update(): Unit = {
    val matchText = contains.value.toLowerCase
    for (threads <- model.from(input.value)) {
      val filtered = if (matchText.nonEmpty) {
        threads.filter(_.stackMentions(matchText))
      } else {
        threads
      }
      render(filtered, viewType.value)
    }
  }

  def render(threads: Seq[Thread], viewType: String): Unit = {
    val report = new InMemoryHtmlReport()

    viewType match {
      case "byStack" =>
        for ((stack, threads) <- Analyzer.groupByStack(threads)) {
          report.printGroupWithSameStack(threads)
        }
      case "contention" =>
        val roots = BlockingTree.buildBlockingTree(threads)
        for (root <- roots) {
          report.printTree(root)
        }
      case _ =>
        for (thread <- threads) {
          report.printTree(thread)
        }
    }

    report.printHeading("Threads in each state")
    report.printStateSummary(Analyzer.groupByState(threads))

    output.innerHTML = report.render
  }
}
