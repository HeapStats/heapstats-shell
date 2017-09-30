/*
 * Copyright (C) 2016-2017 Yasumasa Suenaga
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

/* Settings */
/set mode heapstats -command
/set prompt heapstats "heapstats> " "-> "


/* Imports */
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.time.*;
import java.time.format.*;

import jp.co.ntt.oss.heapstats.task.*;
import jp.co.ntt.oss.heapstats.snapshot.*;
import jp.co.ntt.oss.heapstats.container.log.*;
import jp.co.ntt.oss.heapstats.container.snapshot.*;
import jp.co.ntt.oss.heapstats.container.threadrecord.*;
import jp.co.ntt.oss.heapstats.container.threadrecord.ThreadStat.*;


/* Global variables */
ParseLogFile logParser = null;
ParseHeader snapshotParser = null;
ThreadRecordParseTask threadrecordParser = null;


/* Command declarations */

/**** Resource Log ****/
void openResourceLog(String file){
  openResourceLogList(Arrays.asList(new File(file)));
}

// Avoid JDK-8129843
void openResourceLogList(List<File> files){
  logParser = new ParseLogFile(files, false);
  logParser.run();

  System.out.println("ResourceLog parser is ready.");
}

void resourceLogList(){
  logParser.getLogEntries()
           .stream()
           .forEachOrdered(e -> System.out.println(
                e.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
}

void javaCPU(){
  javaCPUWithRange(LocalDateTime.MIN, LocalDateTime.MAX);
}

// Avoid JDK-8129843
void javaCPUWithRange(LocalDateTime start, LocalDateTime end){
  System.out.println("Java CPU:");
  System.out.println("date time,  %user,  %sys");

  logParser.getDiffEntries()
           .stream()
           .filter(d -> d.getDateTime().isAfter(start) &&
                        d.getDateTime().isBefore(end))
           .forEachOrdered(d -> System.out.printf("%s: %.2f %.2f\n",
                  d.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                  d.getJavaUserUsage(),
                  d.getJavaSysUsage()));
}

void systemCPU(){
  systemCPUWithRange(LocalDateTime.MIN, LocalDateTime.MAX);
}

// Avoid JDK-8129843
void systemCPUWithRange(LocalDateTime start, LocalDateTime end){
  System.out.println("System CPU:");
  System.out.println("date time,  %user,  %nice,  %sys,  %iowait,  %irq,  %softirq,  %steal,  %guest,  %idle");

  logParser.getDiffEntries()
           .stream()
           .filter(d -> d.getDateTime().isAfter(start) &&
                        d.getDateTime().isBefore(end))
           .forEachOrdered(d -> System.out.printf("%s: %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f\n",
                  d.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                  d.getCpuUserUsage(), d.getCpuNiceUsage(), d.getCpuSysUsage(),
                  d.getCpuIOWaitUsage(), d.getCpuIRQUsage(),
                  d.getCpuSoftIRQUsage(), d.getCpuStealUsage(),
                  d.getCpuGuestUsage(), d.getCpuIdleUsage()));
}

void memories(){
  memoriesWithRange(LocalDateTime.MIN, LocalDateTime.MAX);
}

// Avoid JDK-8129843
void memoriesWithRange(LocalDateTime start, LocalDateTime end){
  System.out.println("Java Memory:");
  System.out.println("date time,  VSZ (MB),  RSS (MB)");

  logParser.getLogEntries()
           .stream()
           .filter(d -> d.getDateTime().isAfter(start) &&
                        d.getDateTime().isBefore(end))
           .forEachOrdered(d -> System.out.printf("%s: %d %d\n",
             d.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
             d.getJavaVSSize() / 1024 / 1024, d.getJavaRSSize() / 1024 / 1024));
}

void safepoints(){
  safepointsWithRange(LocalDateTime.MIN, LocalDateTime.MAX);
}

// Avoid JDK-8129843
void safepointsWithRange(LocalDateTime start, LocalDateTime end){
  System.out.println("Safepoints:");
  System.out.println("date time,  count,  time (ms)");

  logParser.getDiffEntries()
           .stream()
           .filter(d -> d.getDateTime().isAfter(start) &&
                        d.getDateTime().isBefore(end))
           .forEachOrdered(d -> System.out.printf("%s: %d %d\n",
                  d.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                  d.getJvmSafepoints(), d.getJvmSafepointTime()));
}

void monitors(){
  monitorsWithRange(LocalDateTime.MIN, LocalDateTime.MAX);
}

// Avoid JDK-8129843
void monitorsWithRange(LocalDateTime start, LocalDateTime end){
  System.out.println("Monitor Contention:");
  System.out.println("date time,  count");

  logParser.getDiffEntries()
           .stream()
           .filter(d -> d.getDateTime().isAfter(start) &&
                        d.getDateTime().isBefore(end))
           .forEachOrdered(d -> System.out.printf("%s: %d\n",
                  d.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                  d.getJvmSyncPark()));
}

void threads(){
  threadsWithRange(LocalDateTime.MIN, LocalDateTime.MAX);
}

// Avoid JDK-8129843
void threadsWithRange(LocalDateTime start, LocalDateTime end){
  System.out.println("Live threads:");
  System.out.println("date time,  count");

  logParser.getLogEntries()
           .stream()
           .filter(d -> d.getDateTime().isAfter(start) &&
                        d.getDateTime().isBefore(end))
           .forEachOrdered(d -> System.out.printf("%s: %d\n",
                  d.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                  d.getJvmLiveThreads()));
}


/**** Snapshot ****/
void openSnapShot(String file){
  openSnapShotList(Arrays.asList(file));
}

// Avoid JDK-8129843
void openSnapShotList(List<String> files){
  snapshotParser = new ParseHeader(files, true, false);
  snapshotParser.run();

  System.out.println("SnapShot parser is ready.");
}

void snapshotList(){
  snapshotParser.getSnapShotList()
                .stream()
                .forEachOrdered(e -> System.out.println(
                                      e.getSnapShotDate().format(
                                       DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
}

void showClassHistogram(SnapShotHeader header, int limit){
  System.out.println("Tag\tClass\tClassLoader\tInstances\tSize(KB)");

  header.getSnapShot(true)
        .values()
        .stream()
        .sorted((o1, o2) -> Long.compare(o2.getTotalSize(), o1.getTotalSize()))
        .limit(limit)
        .map(o -> (new StringJoiner("\t")).add(
                                            "0x" + Long.toHexString(o.getTag()))
                                          .add(o.getName())
                                          .add(o.getLoaderName())
                                          .add(Long.toString(o.getCount()))
                                          .add(Long.toString(
                                                       o.getTotalSize() / 1024))
                                          .toString())
        .forEachOrdered(System.out::println);
  System.out.println();
}

void showSnapShotSummary(SnapShotHeader header){
  System.out.println(header.getSnapShotDate().format(
                              DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                     ": Cause: " + header.getCauseString());
  System.out.println("GC Cause: " + header.getGcCause() +
                     ", Full: " + header.getFullCount() +
                     ", Young: " + header.getYngCount() +
                     ", GC Time: " + header.getGcTime() + "ms");
  System.out.printf("Java heap: capacity: %dMB, new: %dMB, old: %dMB\n",
                    header.getTotalCapacity() / 1024 / 1024,
                    header.getNewHeap() / 1024 / 1024,
                    header.getOldHeap() / 1024 / 1024);
  System.out.printf("Metaspace: capacity: %dMB, usage: %dMB\n",
                    header.getMetaspaceCapacity() / 1024 / 1024,
                    header.getMetaspaceUsage() / 1024 / 1024);
  System.out.println("Total instances: " + header.getNumInstances() +
                     ", Total entries: " + header.getNumEntries());
  System.out.println("---------------------------------------------");
  showClassHistogram(header, 5);
  System.out.println();
}

void snapshotSummary(){
  snapshotSummaryWithRange(LocalDateTime.MIN, LocalDateTime.MAX);
}

// Avoid JDK-8129843
void snapshotSummaryWithRange(LocalDateTime start, LocalDateTime end){
  snapshotParser.getSnapShotList()
                .stream()
                .filter(e -> e.getSnapShotDate().isAfter(start) &&
                             e.getSnapShotDate().isBefore(end))
                .forEachOrdered(e -> showSnapShotSummary(e));
}

void classHisto(){
  classHistoWithRange(LocalDateTime.MIN, LocalDateTime.MAX);
}

// Avoid JDK-8129843
void classHistoWithRange(LocalDateTime start, LocalDateTime end){
  snapshotParser.getSnapShotList()
                .stream()
                .filter(e -> e.getSnapShotDate().isAfter(start) &&
                             e.getSnapShotDate().isBefore(end))
                .forEachOrdered(e -> showClassHistogram(e, Integer.MAX_VALUE));
}

void diffHisto(SnapShotHeader from, SnapShotHeader to){
  List<SnapShotHeader> diffTarget = Arrays.asList(from, to);
  DiffCalculator diffCalc = new DiffCalculator(diffTarget, 0,
                                                       false, null, true);
  diffCalc.run();

  System.out.println("DiffData of " +
         from.getSnapShotDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
         " - " +
         to.getSnapShotDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  System.out.println("Tag\tClass\tClassLoader\tInstances\tSize(KB)");

  diffCalc.getLastDiffList()
          .stream()
          .sorted(Comparator.comparingLong(jp.co.ntt.oss.heapstats.container.snapshot.DiffData::getTotalSize).reversed())
          .map(d -> (new StringJoiner("\t")).add("0x" +
                                                 Long.toHexString(d.getTag()))
                                            .add(d.getClassName())
                                            .add(d.getClassLoaderName())
                                            .add(Long.toString(
                                                           d.getInstances()))
                                            .add(Long.toString(
                                                       d.getTotalSize() / 1024))
                                            .toString())
          .forEachOrdered(System.out::println);
}

void classReference(SnapShotHeader header, long tag, boolean isParent){
  Map<Long, ObjectData> snapShot = header.getSnapShot(true);

  System.out.println(
        header.getSnapShotDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  System.out.println("Start: " + snapShot.get(tag).getName());
  System.out.println("Direction: " + (isParent ? "Parent" : "Child"));
  System.out.println("\tTag\tClass\tClassLoader\tInstances\tSize(KB)");

  ReferenceTracker refTracker = new ReferenceTracker(
                               snapShot, OptionalInt.empty(), Optional.empty());

  List<ObjectData> objectList = isParent ? refTracker.getParents(tag, true)
                                         : refTracker.getChildren(tag, true);
  objectList.stream()
            .map(o -> (new StringJoiner("\t")).add("\t")
                                              .add("0x" +
                                                   Long.toHexString(o.getTag()))
                                              .add(o.getName())
                                              .add(o.getLoaderName())
                                              .add(Long.toString(o.getCount()))
                                              .add(Long.toString(
                                                       o.getTotalSize() / 1024))
                                              .toString())
            .forEachOrdered(System.out::println);
}


/**** Thread Recorder ****/
void openThreadRecord(String file){
  threadrecordParser = new ThreadRecordParseTask(new File(file));
  threadrecordParser.run();

  System.out.println("ThreadRecord parser is ready.");
}

void showThreadIdMap(){
  threadrecordParser.getIdMap()
                    .forEach((k, v) ->
                                System.out.println(k.toString() + ": " + v));
}

void showThreadEvents(Set<ThreadEvent> events){
  threadrecordParser.getThreadStatList()
                    .stream()
                    .sorted()
                    .filter(e -> events.contains(e.getEvent()))
                    .forEachOrdered(System.out::println);
}

void showSuspendEvents(){
  showThreadEvents(EnumSet.of(
                      ThreadEvent.MonitorWait, ThreadEvent.MonitorWaited,
                      ThreadEvent.MonitorContendedEnter,
                      ThreadEvent.MonitorContendedEntered,
                      ThreadEvent.ThreadSleepStart, ThreadEvent.ThreadSleepEnd,
                      ThreadEvent.Park, ThreadEvent.Unpark));
}

void showLockEvents(){
  showThreadEvents(EnumSet.of(
                      ThreadEvent.MonitorContendedEnter,
                      ThreadEvent.MonitorContendedEntered,
                      ThreadEvent.Park, ThreadEvent.Unpark));
}

void showIOEvents(){
  showThreadEvents(EnumSet.of(
                      ThreadEvent.FileWriteStart, ThreadEvent.FileWriteEnd,
                      ThreadEvent.FileReadStart, ThreadEvent.FileReadEnd,
                      ThreadEvent.SocketWriteStart, ThreadEvent.SocketWriteEnd,
                      ThreadEvent.SocketReadStart, ThreadEvent.SocketReadEnd));
}


/* Banner */
System.out.println("HeapStats Shell 0.1.0");
System.out.println("Copyright (C) 2016-2017 Yasumasa Suenaga");

