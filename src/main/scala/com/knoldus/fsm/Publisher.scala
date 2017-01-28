package com.knoldus.fsm

import akka.actor.Actor

import scala.math.min

/**
  * Created by harmeet on 15/1/17.
  */
class Publisher(totalNrBooks: Int, nrBooksPerRequest: Int) extends Actor {

  var nrLeft = totalNrBooks

  override def receive = {
    case PublisherRequest => {
      if(nrLeft == 0) {
        sender() ! BookSupplySoldOut
      } else {
        val supply = min(nrBooksPerRequest, nrLeft)
        nrLeft -= supply
        sender() ! new BookSupply(supply)
      }
    }
  }

}
