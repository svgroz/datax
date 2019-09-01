package org.svgroz.files

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.charset.Charset
import java.nio.file.{Paths, StandardOpenOption}
import java.util.concurrent.Future

import akka.actor.Actor
import akka.event.Logging

sealed abstract class FileOperation

case object ReadFile extends FileOperation

case class ReadChunkFuture(future: Future[Integer], buffer: ByteBuffer) extends FileOperation

case class ReadChunk(buffer: ByteBuffer) extends FileOperation

class FileActor(fileName: String) extends Actor {
  private val log = Logging(context.system, this)

  private val attachedFile: AsynchronousFileChannel = AsynchronousFileChannel.open(
    Paths.get(fileName),
    StandardOpenOption.READ
  )

  override def receive: Receive = {
    case ReadFile => readChunk(
      position = 0,
      chunkSize = 2048
    )
    case readChunkFuture: ReadChunkFuture => handleReadChunkFuture(readChunkFuture)
    case readChunk: ReadChunk => handleReadChunk(readChunk)
    case x => log.error(s"Unsupported operation $x")
  }

  def readChunk(position: Long, chunkSize: Int): Unit = {
    val buffer = ByteBuffer.allocate(chunkSize)
    val futureRead = attachedFile.read(buffer, position)
    self ! ReadChunkFuture(futureRead, buffer)
  }

  def handleReadChunkFuture(readChunkFuture: ReadChunkFuture): Unit = {
    if (readChunkFuture.future.isDone) {
      // chunk read? great, send next message to self or maybe next actor with data parser
      self ! ReadChunk(readChunkFuture.buffer)
    } else if (!readChunkFuture.future.isDone) {
      // resend if future uncompleted
      self ! readChunkFuture
    } else if (readChunkFuture.future.isCancelled) {
      // TODO: something wrong? log or maybe fast fail?
      log.warning(readChunkFuture + " was canceled, file reading cannot be completed")
    }
  }

  def handleReadChunk(readChunk: ReadChunk): Unit = {
    val bytes = readChunk.buffer.array()
    val str = new String(bytes, Charset.forName("UTF-8"))
    log.info(str)
  }
}
