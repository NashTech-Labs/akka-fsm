package com.knoldus.fsm

/**
  * Created by harmeet on 14/1/17.
  */
sealed trait State
case object WaitForRequests extends State
case object ProcessRequest extends State
case object WaitForPublisher extends State
case object SoldOut extends State
case object ProcessSoldOut extends State

case class StateData(nrBookInStore: Int, pendingRequests: Seq[BookRequest])