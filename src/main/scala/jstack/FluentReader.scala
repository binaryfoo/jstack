package jstack

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.FileReader

import scala.concurrent.{Future, Promise}

object FluentReader {

  def readText(file: dom.raw.File): Future[String] = {
    val p = Promise[String]()
    val reader = new FileReader()
    reader.onload = (e: Event) => {
      p.success(reader.result.asInstanceOf[String])
    }
    reader.readAsText(file)
    p.future
  }
}
