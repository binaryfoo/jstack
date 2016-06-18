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
