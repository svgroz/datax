package org.svgroz

import akka.actor.{Actor, ActorSystem, Props}
import org.svgroz.files.{FileActor, ReadFile}

class HelloWorldActor extends Actor {
  override def receive: Receive = {
    case "hello" => println("Hello world")
    case _ => println("Undefined message")
  }
}

object Datax {
  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem("system")
    val helloWorldActor = actorSystem.actorOf(Props[HelloWorldActor], name = "hello_world_actor")
    val fileActor = actorSystem.actorOf(Props(new FileActor("/home/svgroz/examples.desktop")), "file_actor")
    fileActor ! ReadFile
  }
}
