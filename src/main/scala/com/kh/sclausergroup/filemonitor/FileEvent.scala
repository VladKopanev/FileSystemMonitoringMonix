package com.kh.sclausergroup.filemonitor

trait FileEvent

case class FileCreated(fileName: String) extends FileEvent
case class FileChanged(fileName: String) extends FileEvent
case class FileDeleted(fileName: String) extends FileEvent
