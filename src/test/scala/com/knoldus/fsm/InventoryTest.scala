package com.knoldus.fsm

import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack, Transition}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

import scala.concurrent.duration._

/**
  * Created by harmeet on 15/1/17.
  */
class InventoryTest extends TestKit(ActorSystem("InventoryTest")) with WordSpecLike
  with BeforeAndAfterAll with MustMatchers {

  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(3 seconds)

  "Inventory" must {
    "follow the flow" in {
      val publisher = system.actorOf(Props(new Publisher(2, 2)))
      val inventory = system.actorOf(Props(new Inventory(publisher)))

      val stateProbe = TestProbe()
      inventory ! new SubscribeTransitionCallBack(stateProbe.ref)
      stateProbe.expectMsg(new CurrentState(inventory, WaitForRequests))

      val replyProbe = TestProbe()
      inventory ! new BookRequest("Akka", replyProbe.ref)

      stateProbe.expectMsg(new Transition(inventory, WaitForRequests, WaitForPublisher))
      stateProbe.expectMsg(new Transition(inventory, WaitForPublisher, ProcessRequest))
      stateProbe.expectMsg(new Transition(inventory, ProcessRequest, WaitForRequests))
      replyProbe.expectMsg(new BookReply("Akka", Right(1)))

      inventory ! "Some Anonymous Message"
    }
  }
}
