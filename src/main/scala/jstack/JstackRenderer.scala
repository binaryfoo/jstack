package jstack

import java.io.{PrintWriter, StringWriter}

import org.scalajs.dom
import x.{BlockingTree, HtmlReport, Parser}

class JstackRenderer(val output: dom.Element) {

  def fromUrl(url: String): Unit = {
    println(s"Retrieving $url")
    val xhr = new dom.XMLHttpRequest()
    xhr.open("GET", url)
    xhr.onload = (e: dom.Event) => {
      if (xhr.status == 200) {
        render(xhr.responseText)
      }
    }
    xhr.send()
  }

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

}
