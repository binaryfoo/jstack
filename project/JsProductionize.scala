import sbt.Keys._
import sbt._

object JsProductionize {
  val mangleIndexHtml = taskKey[Unit]("mangle index.html for production")

  val settings = Seq(
    mangleIndexHtml := {
      val log = streams.value.log
      val target = (crossTarget in Compile).value / "classes" / "index.html"
      IO.writeLines(target,
        IO.readLines(target).map {
          case line if line.contains("../jstack-fastopt.js") =>
            line.replace("../jstack-fastopt.js", "jstack-opt.js")
          case line if line.contains("/workbench.js") =>
            ""
          case line =>
            line
        }
      )
      log.info(s"jsprod: manged $target")
    })
}