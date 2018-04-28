package com.kh.sclausergroup.filemonitor

import cats.Eq

sealed trait FileSystemEvent {
  def fileName: String
}

case class FileCreated(fileName: String) extends FileSystemEvent
case class FileChanged(fileName: String) extends FileSystemEvent
case class FileDeleted(fileName: String) extends FileSystemEvent
case class WatcherClosed(fileName: String) extends FileSystemEvent

object FileSystemEvent {
 implicit def eqInstance: Eq[FileSystemEvent] = Eq.instance((e1, e2) => e1 == e2)
}
