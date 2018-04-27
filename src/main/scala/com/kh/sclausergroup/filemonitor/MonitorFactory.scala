package com.kh.sclausergroup.filemonitor

import java.nio.file.StandardWatchEventKinds._
import java.nio.file._

import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MonitorFactory extends App {

  Await.result(makeFileMonitor("/home/user/Загрузки").foreachL(println).runAsync, Duration.Inf)

  def makeFileMonitor(path: String): Observable[FileEvent] = {
    val watcher = FileSystems.getDefault.newWatchService()
    val key = Paths.get(path).register(watcher, Array[WatchEvent.Kind[_]](ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE))

    def waitForEvents(): List[FileEvent] = {
      val t = watcher.take()
      t.reset()
      t.pollEvents().asScala.map { we =>
        we.kind() match {
          case ENTRY_CREATE => FileCreated(we.context().toString)
          case ENTRY_MODIFY => FileChanged(we.context().toString)
          case ENTRY_DELETE => FileDeleted(we.context().toString)
        }

      }.toList
    }

    for {
      events <- Observable.repeatEval(waitForEvents())
      e <- Observable.fromIterable(events)
    } yield e
  }
}
