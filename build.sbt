import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

name := "gocd-support"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "org.scala-js" %%% "scalajs-tools" % "0.6.9",
  "com.github.karasiq" %%% "scalajs-bootstrap" % "1.0.5",
  "com.lihaoyi" %%% "scalatags" % "0.5.4",
  "org.scalatest" %%% "scalatest" % "3.0.0-M15" % "test"
)

skip in packageJSDependencies := false

scalaJSUseRhino in Global := false

bootSnippet := "jstack.JsMain().main(document, window.location.search);"

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)