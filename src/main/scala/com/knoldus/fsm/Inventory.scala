package com.knoldus.fsm

import akka.actor.FSM.Normal
import akka.actor.{Actor, ActorRef, FSM}

/**
  * Created by harmeet on 14/1/17.
  */
class Inventory(publisher: ActorRef) extends Actor with FSM[State, StateData] {

  var reserveId = 0;

  startWith(WaitForRequests, new StateData(0, Seq()))

  when(WaitForRequests) {
      // In this state BookRequest or PendingRequests message can arrive
    case Event(request: BookRequest, data: StateData) => {
      val newStateData = data.copy(
        pendingRequests = data.pendingRequests :+ request
      )
      if(newStateData.nrBookInStore > 0){
        goto(ProcessRequest) using newStateData // Declare the next state ProcessRequest with new state data
      } else {
        goto(WaitForPublisher) using newStateData
      }
    }
    case Event(PendingRequests , data: StateData) => {
      if(data.pendingRequests.isEmpty) {
        stay()                                // Declare the state doesn't change
      } else if(data.nrBookInStore > 0) {
        goto(ProcessRequest)
      } else {
        goto(WaitForPublisher)
      }
    }
  }

  //fallback handler when an Event is unhandled by none of the States.
  whenUnhandled {
    case Event(request: BookRequest, data: StateData) => {
      stay() using data.copy(
        pendingRequests = data.pendingRequests :+ request
      )
    }

    case Event(e, s) => {
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stop()
    }
  }

  when(WaitForPublisher) {
    case Event(supply: BookSupply, data: StateData) => {
      goto(ProcessRequest) using data.copy(
        nrBookInStore = supply.nrBooks
      )
    }
    case Event(BookSupplySoldOut, _) => {
      goto(ProcessSoldOut)
    }
  }

  when(ProcessRequest) {
    case Event(Done, data: StateData) => {
      goto(WaitForRequests) using data.copy(
        nrBookInStore = data.nrBookInStore - 1,
        pendingRequests = data.pendingRequests.tail
      )
    }
  }

  when(SoldOut) {
    case Event(request: BookRequest, data: StateData) => {
      goto(ProcessSoldOut) using new StateData(0, Seq(request))
    }
  }

  when(ProcessSoldOut) {
    case Event(Done, data: StateData) => {
      goto(SoldOut) using new StateData(0, Seq())
    }
  }

  // Transition from one event to another.
  onTransition {
    case _ -> WaitForRequests => {
      log.info(s"In WaitForRequests Transitions .. ")

      if (!nextStateData.pendingRequests.isEmpty) {
        //go to next state
        self ! PendingRequests
      }
    }

    case _ -> WaitForPublisher => {
      log.info(s"In WaitForPublisher Transitions .. ")

      //send request to publisher
      publisher ! PublisherRequest
    }

    case _ -> ProcessRequest => {
      log.info(s"In ProcessRequest Transitions .. ")

      nextStateData.pendingRequests.foreach { request =>
        reserveId += 1
        request.target ! new BookReply(request.bookName, Right(reserveId))
      }
      self ! Done
    }

    case _ -> ProcessSoldOut => {
      log.info(s"In ProcessSoldOut Transitions .. ")

      nextStateData.pendingRequests.foreach { request =>
        request.target ! new BookReply(request.bookName, Left("SoldOut"))
      }
      self ! Done
    }
  }

  //
  onTermination {
    case StopEvent(Normal, state, data) => {
      log.info(s"Termination in normal way in $state state with $data data.")
    }
  }

  initialize() // Method is used to initialize and startup the FSM
}
