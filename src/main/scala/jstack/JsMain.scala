package jstack

import java.io.{PrintWriter, StringWriter}

import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.FileReader
import x.{BlockingTree, HtmlReport, Parser}


@JSExport
object JsMain {

  @JSExport
  def main(doc: html.Document) {
    val form = doc.getElementById("stackForm").asInstanceOf[html.Form]
    val input = doc.getElementById("stackInput").asInstanceOf[html.TextArea]
    val output = doc.getElementById("output")

    def render(value: String): Unit = {
      val lines = value.split("\n")
      val threads = Parser.parse(lines.iterator)
      val roots = BlockingTree.buildBlockingTree(threads)
      val out = new StringWriter()
      val writer = new PrintWriter(out)
      val report = new HtmlReport(writer)
      for (root <- roots) {
        report.printTree(root)
      }
      writer.close()
      output.innerHTML = out.toString
    }

    input.addEventListener("dragenter", (e: dom.Event) => {
      e.stopPropagation()
      e.preventDefault()
    }, false)

    input.addEventListener("dragover", (e: dom.Event) => {
      e.stopPropagation()
      e.preventDefault()
    }, false)

    input.addEventListener("drop", (e: dom.DragEvent) => {
      e.stopPropagation()
      e.preventDefault()

      val reader = new FileReader()
      reader.onload = (e: dom.Event) => {
        val contents = reader.result.asInstanceOf[String]
        render(contents)
      }
      val item = e.dataTransfer.files.item(0)
      println(s"reading $item")
      reader.readAsText(item)
    }, false)

    form.onsubmit = (e: dom.Event) => {
      e.preventDefault()
      render(input.value)
    }
  }
}
