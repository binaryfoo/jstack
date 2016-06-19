import com.lihaoyi.workbench.Plugin._
import com.typesafe.sbt.site.util.SiteHelpers
import JsProductionize._

enablePlugins(ScalaJSPlugin)

workbenchSettings

JsProductionize.settings

name := "jstack"

organization := "io.github.binaryfoo"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "org.scala-js" %%% "scalajs-tools" % "0.6.9",
  "com.github.karasiq" %%% "scalajs-bootstrap" % "1.0.5",
  "com.lihaoyi" %%% "scalatags" % "0.5.4",
  "org.scalatest" %%% "scalatest" % "3.0.0-RC2" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0-RC2" % "test"
)

skip in packageJSDependencies := false

scalaJSUseRhino in Global := false

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)

mangleIndexHtml <<= mangleIndexHtml.triggeredBy(fullOptJS in Compile)

bootSnippet := "jstack.JsMain().main(document, window.location.search);"

siteMappings ++= SiteHelpers.selectSubpaths(crossTarget.value / "classes", "*.html" | "*.css" | "*.js")

siteMappings ++= SiteHelpers.selectSubpaths(crossTarget.value, "*.js" | "js.map")

ghpages.settings

git.remoteRepo := "git@github.com:binaryfoo/jstack.git"
