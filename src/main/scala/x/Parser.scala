package x

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.matching.Regex.Groups

/**
  * Dumped by go's com.thoughtworks.go.server.service.support.ServerRuntimeInformationProvider#threadInfo()
  *
  * @param instance The lock.
  * @param frame Stack frame where lock was taken (class name and method).
  * @param sourceReference Line where lock holder's execution path meets the stack frame where the lock was taken.
  */
case class LockedMonitor(instance: String, frame: String, sourceReference: String) {
  def isSameFrame(other: String): Boolean = other.contains(frame)
}

/**
  * A single stack trace
  */
case class Thread(id: String, name: String, state: String, stack: Seq[String] = Seq.empty, monitors: Seq[LockedMonitor] = Seq.empty) {

  def blockedOn(held: Seq[LockedMonitor]): Boolean = {
    state == "BLOCKED" && stack.headOption.exists(frame => held.exists(_.isSameFrame(frame)))
  }

  def hasLockFromFrame(frame: String): Boolean = {
    monitors.exists(_.isSameFrame(frame))
  }
}

object ParserState extends Enumeration {
  var StackTrace, LockedMonitors, Ignore = Value
}

object Parser {
  val ThreadDetails = """(\d+), ([^,]+), (.+)""".r
  val LockedMonitorDetails = """(.*?) at ([^(]+)\(([^)]+)\)""".r

  def parse(fileName: String): Seq[Thread] = {
    import ParserState._
    var phase = Ignore
    var thread: Thread = null
    var threads = ArrayBuffer[Thread]()

    for (line <- Source.fromFile(fileName).getLines().dropWhile(line => !line.startsWith("All thread stacktraces"))) {
      line match {
        case ThreadDetails(id, name, state) =>
          if (thread != null)
            threads += thread
          thread = Thread(id, name, state)
        case "Locked Monitors:" =>
          phase = LockedMonitors
        case "Stacktrace:" =>
          phase = StackTrace
        case "" =>
          phase = Ignore
        case _ if phase == LockedMonitors && line != "Locked Synchronizers:" =>
          thread = thread.copy(monitors = parseMonitors(line))
        case _ if phase == StackTrace =>
          thread = thread.copy(stack = thread.stack :+ line.trim)
        case _ =>
      }
    }
    threads
  }

  def parseMonitors(line: String): Seq[LockedMonitor] = {
    LockedMonitorDetails.findAllMatchIn(line).map {
      case Groups(a, b, c) =>
        LockedMonitor(a, b, c)
    }.toSeq
  }
}
