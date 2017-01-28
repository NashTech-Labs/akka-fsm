package com.knoldus.fsm

import akka.actor.ActorRef

/**
  * Created by harmeet on 14/1/17.
  */
case class BookRequest(bookName: String, target: ActorRef)
case class BookSupply(nrBooks: Int)
case object BookSupplySoldOut
case object Done
case object PendingRequests