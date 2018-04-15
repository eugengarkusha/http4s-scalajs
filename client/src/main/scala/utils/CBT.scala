package utils

import japgolly.scalajs.react.{Callback, CallbackTo}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext
import scala.util.{Failure, Success}


case class CBT[V] private (cbf: CallbackTo[Future[V]]) {

  implicit def ec: ExecutionContext = CBT.executionContext

  def map[R](f: V => R): CBT[R] = CBT(cbf.map(_.map(f)))

  def flatMap[R](f: V => CBT[R]): CBT[R] = {
    CBT(cbf.flatMap(fut => CallbackTo.future(fut.map(f.andThen(_.cbf))).map(_.flatten)))
  }

  def >>[R](v: CBT[R]): CBT[R] = flatMap(_ => v)
  def <<(v: CBT[_]): CBT[V] = flatMap(x => v.map(_ => x))

  def recover(f: Throwable => V): CBT[V] = CBT(cbf.attempt.map(_.fold(f.andThen(Future.successful), _.recover {
    case t: Throwable => f(t)
  })))

  def onError(f: Throwable => CBT[Unit]): CBT[V] = {
    def onErr(err: Throwable): CBT[V] = f(err).flatMap(_ => CBT.raiseError[V](err))

    CBT(
      cbf.attempt.flatMap(
        _.fold(
          err => onErr(err).cbf,
          t => CBT.fromFut(
            t.map(CBT.pure).recover {
              case err: Throwable => onErr(err)
            }
          ).flatten.cbf
        )
      )
    )
  }

  def flatten[R](implicit ev: V <:< CBT[R]): CBT[R] = flatMap(ev)

  def ensure(cond: V => Boolean)(err: => ErrorMagnet): CBT[V] = flatMap(v => if (cond(v)) CBT.pure(v) else err.raise[V])

  //Its required to explicitly rethrow exception. ScalaJs-react will not do it by default(see Callback.future doc)
  def cb: Callback = cbf.map(_.onComplete(_.fold[Unit](throw _, identity[V])))

}

object CBT {

  implicit val executionContext: ExecutionContext = JSExecutionContext.queue

  //todo: make universal trait to avoid runtime allocation
  trait ErrorMagnet {
    def raise[T]: CBT[T]
  }

  implicit class StringErr(val err: String) extends ErrorMagnet {
    def raise[T]: CBT[T] = CBT(CallbackTo(Future.failed(new Exception(err))))
  }

  implicit class ThrowableErr(val err: Throwable) extends ErrorMagnet {
    def raise[T]: CBT[T] = CBT(CallbackTo(Future.failed(err)))
  }

  def fromFut[T](f: => Future[T]): CBT[T] = CBT(CallbackTo.future(f.map(CallbackTo(_))))

  def raiseError[T](e: ErrorMagnet): CBT[T] = e.raise[T]

  def pure[T](t: T) = CBT(CallbackTo(Future.successful(t)))

  def fromCB[T](f: CallbackTo[T]): CBT[T] = CBT(f.map(Future.successful))

  implicit class CallBackOps[T](cb: CallbackTo[T]) {
    def cbt: CBT[T] = fromCB(cb)

    //TODO:  Rethrow exception here to preserve the callback type(it is ok, its used in react lib code(see Callback.future))
    //TODO: test this method. Highly suspect exception not to be extracted from try(so need rethrow)
    def onError(f: Throwable => Callback): Callback = {
      cb.attempt.flatMap(_.fold(t => f(t).map(_ => Failure(t): Unit), r => CallbackTo(Success(r): Unit)))
    }
  }

  implicit class FutureCallbackOps[T](f: => Future[CallbackTo[T]]) {
    def cbt: CBT[T] = CBT(CallbackTo.future(f))

    def callback: Callback = Callback.future(f)
  }

  implicit class FutureOps[T](f: => Future[T]) {
    def cbt: CBT[T] = fromFut(f)

    def callback: Callback = Callback.future(f.map(Callback(_)))
  }

}

