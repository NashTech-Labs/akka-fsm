package com.knoldus.fsm

/**
  * Created by harmeet on 15/1/17.
  */
case object PublisherRequest
case class BookReply(context: AnyRef, reserveId: Either[String, Int])