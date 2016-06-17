package jstack

import jstack.UrlParser.parseQueryParams
import org.scalajs.dom.html.{Form, TextArea}
import org.scalajs.dom.raw.FileReader
import org.scalajs.dom.{DragEvent, Event, html}

import scala.scalajs.js.annotation.JSExport

@JSExport
object JsMain {

  val Url = """(https?://[^ ]+)""".r.unanchored

  @JSExport
  def main(doc: html.Document, search: String) {
    val form = doc.getElementById("stackForm").asInstanceOf[Form]
    val input = doc.getElementById("stackInput").asInstanceOf[TextArea]
    val output = doc.getElementById("output")
    val renderer = new JstackRenderer(output)

    configureDragAndDropFile(input, renderer)

    form.onsubmit = (e: Event) => {
      e.preventDefault()

      input.value match {
        case Url(url) =>
          renderer.fromUrl(url)
        case text =>
          renderer.render(text)
      }
    }

    for (url <- parseQueryParams(search).get("url")) {
      input.value = url
      renderer.fromUrl(url)
    }
  }

  def configureDragAndDropFile(input: TextArea, renderer: JstackRenderer): Unit = {
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

      val reader = new FileReader()
      reader.onload = (e: Event) => {
        val contents = reader.result.asInstanceOf[String]
        renderer.render(contents)
      }
      val item = e.dataTransfer.files.item(0)
      reader.readAsText(item)
    }, false)
  }

}
