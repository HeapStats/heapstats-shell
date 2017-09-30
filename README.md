# HeapStats Shell

## Requirements

* JDK 9
* `heapstats-core.jar`
    * https://mvnrepository.com/artifact/jp.co.ntt.oss.heapstats/heapstats-core
    * `heapstats-shell` will download this JAR via Maven.

## How to use

```
$ heapstats-shell
```

or

```
$ jshell --class-path target/dependency/heapstats-core*.jar --feedback heapstats heapstats.jsh
```

## Command reference

* Resource Log (CSV)
    * `openResourceLog(String file)`
        * Open HeapStats CSV file.
    * `openResourceLogList(List<File> files)`
        * Open HeapStats CSV files.
    * `resourceLogList()`
        * Show all log entries.
    * `javaCPU()`
    * `javaCPUWithRange(LocalDateTime start, LocalDateTime end)`
        * Show all Java CPU usage.
    * `systemCPU()`
    * `systemCPUWithRange(LocalDateTime start, LocalDateTime end)`
        * Show all system CPU usage.
    * `memories()`
    * `memoriesWithRange(LocalDateTime start, LocalDateTime end)`
        * Show all memory usage.
    * `safepoints()`
    * `safepointsWithRange(LocalDateTime start, LocalDateTime end)`
        * Show all safepoints.
    * `monitors()`
    * `monitorsWithRange(LocalDateTime start, LocalDateTime end)`
        * Show all monitor events.
    * `threads()`
    * `threadsWithRange(LocalDateTime start, LocalDateTime end)`
        * Show all number of live threads.
* SnapShot
    * `openSnapShot(String file)`
        * Open HeapStats SnapShot.
    * `openSnapShotList(List<String> files)`
        * Open HeapStats SnapShots.
    * `snapshotList()`
        * Show SnapShot list.
    * `snapshotSummary()`
    * `snapshotSummaryWithRange(LocalDateTime start, LocalDateTime end)`
        * Show SnapShot summary.
    * `classHisto()`
    * `classHistoWithRange(LocalDateTime start, LocalDateTime end)`
        * Show class histogram.
    * `diffHisto(SnapShotHeader from, SnapShotHeader to)`
        * Show class histogram differences between `from` and `to` .
    * `classReference(SnapShotHeader header, long tag, boolean isParent)`
        * Show class references.
* Thread Recorder
    * `openThreadRecord(String file)`
        * Open HeapStats Thread Recorder file.
    * `showThreadIdMap()`
        * Show thread ID and name map.
    * `showSuspendEvents()`
        * Show thread suspend events.
    * `showLockEvents()`
        * Show thread locking event.
    * `showIOEvents()`
        * Show IO events.

## TODO

* Currently, We cannot define jshell command. We will define current methods in `heapstats.jsh` as commands if it can.
    * [JDK-8157208](https://bugs.openjdk.java.net/browse/JDK-8157208): jshell tool: pluggable commands
* [JDK-8129843](https://bugs.openjdk.java.net/browse/JDK-8129843) reports we cannot define overload method(s). After this bug is fixed, we will define `*WithRange` methods as overloaded methods.

## License

GNU General Public License, version 2
