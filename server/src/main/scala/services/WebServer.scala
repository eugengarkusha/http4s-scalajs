package services

import java.util.concurrent.ScheduledThreadPoolExecutor

import cats.effect.IO
import fs2._
import org.http4s._
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global
//import org.http4s.implicits._
//import cats.data.{Kleisli, OptionT}
//import cats.implicits._
//import cats.instances.

object WebServer extends App{




  implicit val scheduler = Scheduler.fromScheduledExecutorService(new ScheduledThreadPoolExecutor(2))
  val service: BootStrapService[IO] = new BootStrapService[IO]
  val authService: AuthedService[IO] = new AuthedService[IO]

  val s1: HttpService[IO] = service.rootService
  val s2: HttpService[IO] = authService.service



  val x = BlazeBuilder[IO].bindHttp(9001, "localhost").mountService(s1 <+> s2, "/").start.unsafeToFuture.map(s =>{println(s.baseUri);s})
  println(s"ServerX is online! localhost:9001")
//  println(System.in.read(Array.ofDim(199)))
//  x.map(_.shutdownNow())
  Thread.sleep(1099092013)



}
