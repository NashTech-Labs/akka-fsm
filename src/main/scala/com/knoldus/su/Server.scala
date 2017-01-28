package com.knoldus.su

import akka.actor.{Actor, ActorSystem, Props, Stash}
import com.knoldus.su.Server.{Connect, Disconnect, Request}

/**
  * Created by harmeet on 28/1/17.
  */

// An example of conditional based actor state using stash
class Server extends Actor with Stash {

  var online = true

  override def receive = {
    case Request => {
      if(online) processRequest else stash()
    }
    case Connect => {
      println(s"resources up")
      online = true
      unstashAll()
    }

    case Disconnect => {
      println(s"resources down")
      online = false
    }
  }

  private def processRequest = println(s"request process successfully")
}

object Server {
  case object Connect
  case object Disconnect
  case object Request

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("Server")
    val ref = system.actorOf(Props[Server])
    ref ! Request
    ref ! Request
    ref ! Disconnect
    ref ! Request
    ref ! Request
    ref ! Connect
  }
}