package com.knoldus.bu

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.knoldus.bu.Switch.{OFF, ON}

/**
  * Created by harmeet on 14/1/17.
  */

// An example of become()/unbecome() in akka
class Switch extends Actor with ActorLogging {

  def on: Receive = {
    case ON =>
      log.warning("Received on while already in ON state")
    case OFF => context.unbecome()
  }

  def off: Receive = {
    case ON => context.become(on)
    case OFF =>
      log.warning("Received on while already in OFF state")
  }
  override def receive = off
}

object Switch {
  case object ON
  case object OFF

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("Switch")
    val switch = system.actorOf(Props[Switch])
    switch ! OFF
    switch ! OFF
    switch ! ON
    switch ! ON
    switch ! OFF
    switch ! OFF
  }
}
