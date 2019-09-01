package org.svgroz.files

import akka.actor.Actor
import akka.event.Logging

class FileMapperActor extends Actor {
  private val log = Logging(context.system, this)

  override def receive: Receive = {
    case readChunk: ReadChunk => handleReadChunk(readChunk)
    case unsupported => log.error("Unsupported message ({}) from sender {}", unsupported, sender())
  }

  def handleReadChunk(readChunk: ReadChunk): Unit = {

  }
}
