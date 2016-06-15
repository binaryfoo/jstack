package x

case class BlockingTree(thread: Thread, blocked: Seq[BlockingTree]) {

  /**
    * Group threads in blocked by stack trace
    */
  def summarise(): DeDuplicatedBlockingTree = {
    val (leaves, internal) = blocked.partition(_.blocked.isEmpty)
    val directlyBlocked = leaves.groupBy(_.thread.stack).map {
      case (stack, blockedThreads) =>
        BlockedSet(stack, blockedThreads.map(_.thread))
    }.toSeq
    DeDuplicatedBlockingTree(thread, directlyBlocked, internal.map(_.summarise()))
  }

}

/**
  * Set of blocked threads sharing a common stack trace
  */
case class BlockedSet(stack: Seq[String], threads: Seq[Thread])

case class DeDuplicatedBlockingTree(lockHolder: Thread, blocked: Seq[BlockedSet], children: Seq[DeDuplicatedBlockingTree]) {

  def totalChildren: Int = {
    val direct = blocked.map(_.threads.size).sum
    val transitive = children.map(_.totalChildren).sum
    direct + transitive
  }

  def dump(indent: String = "", sb: StringBuilder = new StringBuilder): String = {
    val nextIndent = "  " + indent
    val stackIndent = "  " + nextIndent
    sb.append(indent).append(lockHolder.name).append(" {").append(lockHolder.state).append("}")
    for (b <- blocked) {
      sb.append("\n")
      sb.append(nextIndent).append(b.threads.size).append(": ").append(b.threads.map(_.name).mkString("[", ", ", "]")).append("\n")
      sb.append(b.stack.take(10).map(frame => stackIndent + frame).mkString("\n"))
    }
    for (c <- children) {
      c.dump(nextIndent, sb)
    }
    sb.toString()
  }
}

object BlockingTree {

  def build(edges: Seq[(Thread, Thread)]): Seq[BlockingTree] = {
    val roots = for ((_, lockHolder) <- edges.toSet if !edges.exists(_._1 == lockHolder)) yield lockHolder
    for (root <- roots.toSeq)
      yield collectThreadsBlocked(root, edges)
  }

  def collectThreadsBlocked(root: Thread, edges: Seq[(Thread, Thread)]): BlockingTree = {
    val children = for ((blocked, lockHolder) <- edges if lockHolder == root)
      yield collectThreadsBlocked(blocked, edges)
    BlockingTree(root, children)
  }
}
