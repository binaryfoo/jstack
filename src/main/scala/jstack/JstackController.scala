package jstack

import java.io.{PrintWriter, StringWriter}

import io.github.binaryfoo.yatal.{BlockingTree, HtmlReport, Thread}
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global

case class JstackController(model: JstackModel, output: dom.Element, input: dom.html.TextArea, viewType: dom.html.Input) {

  def update(): Unit = {
    for (threads <- model.from(input.value)) {
      render(threads, viewType.value)
    }
  }

  def render(threads: Seq[Thread], viewType: String): Unit = {
    val out = new StringWriter()
    val writer = new PrintWriter(out)
    val report = new HtmlReport(writer)

    viewType match {
      case "byStack" =>
        for ((stack, threads) <- threads.groupBy(_.stack).toSeq.sortBy(_._2.size).reverse) {
          val states = threads.map(_.state).toSet.mkString("{", ",", "}")
          report.printTree(threads.size + " " + states, threads)
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

    writer.close()
    output.innerHTML = out.toString
  }
}
