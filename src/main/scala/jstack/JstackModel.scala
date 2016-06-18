package jstack

import org.scalajs.dom
import x.Parser

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

class JstackModel {

  val Url = """[ ]+(https?://[^ ]+).*""".r

  def from(urlOrText: String): Future[Seq[x.Thread]] = {
    urlOrText match {
      case Url(url) =>
        fromUrl(url)
      case text =>
        fromText(text)
    }
  }

  def fromUrl(url: String): Future[Seq[x.Thread]] = {
    for {
      text <- fetch(url)
      threads <- fromText(text)
    } yield threads
  }

  def fetch(url: String): Future[String] = {
    println(s"Retrieving $url")
    val p = Promise[String]()
    val xhr = new dom.XMLHttpRequest()
    xhr.open("GET", url)
    xhr.onload = (e: dom.Event) => {
      if (xhr.status == 200) {
        p.success(xhr.responseText)
      } else {
        p.failure(new Exception("Failed to get " + url + ": " + xhr.statusText))
      }
    }
    xhr.send()
    p.future
  }


  def fromText(text: String): Future[Seq[x.Thread]] = {
    Future(Parser.parseText(text))
  }
}
