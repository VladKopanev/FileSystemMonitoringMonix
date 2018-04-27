package com.kh.sclausergroup.filemonitor

trait FileMonitor {

  def onChange[E](action: FileEvent => E): FileMonitor
}
