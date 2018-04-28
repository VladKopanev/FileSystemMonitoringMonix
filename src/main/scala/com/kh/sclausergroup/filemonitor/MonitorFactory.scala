package com.kh.sclausergroup.filemonitor

import java.nio.file.StandardWatchEventKinds._
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn
import scala.util.control.NonFatal

object MonitorFactory extends App {

  val watchFile = StdIn.readLine()

  Await.result(makeFileMonitor(watchFile).foreach(println), Duration.Inf)

  def makeFileMonitor(path: String): Observable[FileSystemEvent] = {
    val root = Paths.get(path)
    val watcher = root.getFileSystem.newWatchService()

    registerWatchServiceRecursively(watcher, root)

    (for {
      events <- Observable.repeatEval(waitForEvents(watcher))
      e <- Observable.fromIterable(events)
    } yield e).distinctUntilChanged.onErrorRecover {
      case NonFatal(_) => WatcherClosed(path)
    }
  }

  private def registerWatchServiceRecursively(watcher: WatchService, root: Path): Unit = {
    if(Files.isDirectory(root)) {
      registerWatchService(watcher, root)
      Files.list(root).forEach(f => registerWatchServiceRecursively(watcher, f))
    }
  }

  private def registerWatchService(watcher: WatchService, root: Path): Unit = {
    root.register(watcher, Array[WatchEvent.Kind[_]](ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE))
  }

  private def waitForEvents(watcher: WatchService): List[FileSystemEvent] = {
    val t: WatchKey = watcher.take()
    t.reset()
    t.pollEvents().asScala.map(prepareEvent).toList
  }

  private def prepareEvent(ev: WatchEvent[_]): FileSystemEvent = ev.kind() match {
    case ENTRY_CREATE => FileCreated(ev.context().toString)
    case ENTRY_MODIFY => FileChanged(ev.context().toString)
    case ENTRY_DELETE => FileDeleted(ev.context().toString)
  }
}
