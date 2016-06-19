## What
What's a thread dump and why might I want one? [well put](http://www.ateam-oracle.com/analyzing-thread-dumps-in-middleware-part-1-2/)

> A Thread Dump is a brief snapshot in textual format of threads within a Java Virtual Machine (JVM). This is equivalent to process dump in the native world. Data about each thread including the name of the thread, priority, thread group, state (running/blocked/waiting/parking) as well as the execution stack in form of thread stack trace is included in the thread dump. All threads – the Java VM threads (GC threads/scavengers/monitors/others) as well as application and server threads are all included in the dump. Newer versions of JVMs also report blocked thread chains (like ThreadA is locked for a resource held by ThreadB) as well as deadlocks (circular dependency among threads for locks).

## Go support

Parse a thread dump from the /api/support endpoint of [go](https://github.com/gocd/gocd).

Last I looked was produced by [ThreadInformationProvider](https://github.com/gocd/gocd/blob/master/server/src/com/thoughtworks/go/server/service/support/ThreadInformationProvider.java).

Used hunting performance troubles.

### Screenshot

Example from [go-log-12.txt](https://raw.githubusercontent.com/binaryfoo/jstack/master/src/test/resources/go-log-12.txt)

![Screenshot](screenshot.png "Example from go-log-12.txt")

### Other Tools

* [TDA - Thread Dump Analyzer](https://java.net/projects/tda/)
* [Spotify Online Java Thread Dump Analyzer](http://spotify.github.io/threaddump-analyzer/)
* [Atlassian's Instructions](https://confluence.atlassian.com/doc/generating-a-thread-dump-externally-182158040.html)
* [Samurai](http://samuraism.jp/samurai/en/index.html)
* [mchr3k's](https://mchr3k.github.io/javathreaddumpanalyser/)

### Thoughts of others

* [Structured dumping enabling better tooling](http://www.outerthoughts.com/threaddumps/thread_dumps.html) In this case XML, XLST and graphviz.