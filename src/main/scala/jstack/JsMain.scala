package jstack

import jstack.UrlParser.parseQueryParams
import org.scalajs.dom.html.{Form, Input, TextArea}
import org.scalajs.dom.{DragEvent, Event, html}

import scala.scalajs.js.annotation.JSExport
import scala.concurrent.ExecutionContext.Implicits.global

@JSExport
object JsMain {

  @JSExport
  def main(doc: html.Document, search: String) {
    val form = doc.getElementById("stackForm").asInstanceOf[Form]
    val input = doc.getElementById("stackInput").asInstanceOf[TextArea]
    val contains = doc.getElementById("stackContains").asInstanceOf[TextArea]
    val viewType = doc.getElementById("viewType").asInstanceOf[Input]
    val output = doc.getElementById("output")

    val model = new JstackModel()
    val controller = new JstackController(model, output, input, contains, viewType)

    configureDragAndDropFile(input, controller)

    form.onsubmit = (e: Event) => {
      e.preventDefault()
      controller.update()
    }

    viewType.onchange = (e: Event) => {
      controller.update()
    }

    for (url <- parseQueryParams(search).get("url")) {
      input.value = url
      controller.update()
    }
  }

  def configureDragAndDropFile(input: TextArea, controller: JstackController): Unit = {
    input.addEventListener("dragenter", (e: Event) => {
      e.stopPropagation()
      e.preventDefault()
    }, false)

    input.addEventListener("dragover", (e: Event) => {
      e.stopPropagation()
      e.preventDefault()
    }, false)

    input.addEventListener("drop", (e: DragEvent) => {
      e.stopPropagation()
      e.preventDefault()

      for {
        contents <- FluentReader.readText(e.dataTransfer.files.item(0))
      } {
        input.value = contents
        controller.update()
      }
    }, false)
  }

}
