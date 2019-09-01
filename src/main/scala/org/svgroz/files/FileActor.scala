package org.svgroz.files

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousFileChannel, CompletionHandler}
import java.nio.charset.Charset
import java.nio.file.{Paths, StandardOpenOption}

import akka.actor.{Actor, ActorRef}
import akka.event.Logging

class FileActor(fileName: String) extends Actor {
  private val log = Logging(context.system, this)

  private val attachedFile: AsynchronousFileChannel = AsynchronousFileChannel.open(
    Paths.get(fileName),
    StandardOpenOption.READ, StandardOpenOption.WRITE
  )

  override def receive: Receive = {
    case readChunkRequest: ReadChunkRequest => readChunk(readChunkRequest)
    case readChunk: ReadChunk => handleReadChunk(readChunk)
    // TODO: something wrong? log or maybe fast fail?
    case readChunkError: ReadChunkError => handleReadChunkError(readChunkError)
    case unsupported => log.error("Unsupported message ({}) from sender {}", unsupported, sender())
  }

  def readChunk(readChunkRequest: ReadChunkRequest): Unit = {
    val buffer = ByteBuffer.allocate(readChunkRequest.chunkSize)
    attachedFile.read(
      buffer,
      readChunkRequest.position,
      buffer,
      new ReadChunkCompletionHandler(
        self = self,
        requestId = readChunkRequest.requestId
      )
    )
  }

  def handleReadChunkError(readChunkError: ReadChunkError): Unit = {
    log.error("Cant process {}", readChunkError)
  }

  def handleReadChunk(readChunk: ReadChunk): Unit = {
    // TODO send to a next actor
    val bytes = readChunk.data
    val str = new String(bytes, Charset.forName("UTF-8"))
    log.info(str)
  }
}

class ReadChunkCompletionHandler(val self: ActorRef, val requestId: Serializable) extends CompletionHandler[Integer, ByteBuffer] {
  override def completed(readBytes: Integer, attachment: ByteBuffer): Unit = {
    // TODO add "readBytes == -1" condition
    val rawBytes = attachment.array()
    val bytes = new Array[Byte](readBytes)
    System.arraycopy(rawBytes, 0, bytes, 0, readBytes)

    self ! ReadChunk(requestId, bytes)
  }

  override def failed(exc: Throwable, attachment: ByteBuffer): Unit = {
    self ! ReadChunkError(requestId, Some(exc))
  }
}
