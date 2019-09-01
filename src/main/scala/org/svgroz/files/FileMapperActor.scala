package org.svgroz.files

import akka.actor.Actor
import akka.event.Logging

class FileMapperActor extends Actor {
  private val log = Logging(context.system, this)

  override def receive: Receive = {
    case readChunk: ReadChunk =>
    case _ =>
  }

  def handleReadChunk(readChunk: ReadChunk): Unit = {

  }
}
