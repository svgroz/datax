package org.svgroz

sealed trait CoreMessage

case class UnsupportedMessage(source: Any) extends CoreMessage
