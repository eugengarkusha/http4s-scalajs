package com.example.httpo4sscalajs

import java.util.concurrent.ScheduledThreadPoolExecutor

import cats.effect.IO
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import fs2._

object WebServer extends App{


  implicit val scheduler = Scheduler.fromScheduledExecutorService(new ScheduledThreadPoolExecutor(2))
  val service = new WebService[IO]
  val x = BlazeBuilder[IO].bindHttp(9001, "localhost").mountService(service.rootService, "/").start.unsafeToFuture.map(s =>{println(s.baseUri);s})
  println(s"ServerX is online! localhost:9001")
//  println(System.in.read(Array.ofDim(199)))
//  x.map(_.shutdownNow())
  Thread.sleep(1099092013)


}
