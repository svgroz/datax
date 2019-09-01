package org.svgroz.files

sealed trait FileOperation

case class ReadChunkRequest(requestId: Serializable, position: Long = 0, chunkSize: Int = 4) extends FileOperation

case class ReadChunkError(requestId: Serializable, throwable: Option[Throwable]) extends FileOperation

case class ReadChunk(requestId: Serializable, data: Array[Byte]) extends FileOperation
