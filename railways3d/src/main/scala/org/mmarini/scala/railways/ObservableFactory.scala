package org.mmarini.scala.railways

import rx.lang.scala.Observable
import rx.lang.scala.Subject
import rx.lang.scala.Subscription
import com.jme3.collision.CollisionResult
import com.jme3.math.Ray
import com.jme3.collision.CollisionResults
import com.jme3.math.Vector2f
import org.mockito.cglib.proxy.LazyLoader
import com.typesafe.scalalogging.LazyLogging

/**
 * @author us00852
 */

trait PositionMapping {
  def position: Vector2f
}

trait Mapping {
  def name: String
  def tpf: Float
}

case class ActionMapping(name: String, keyPressed: Boolean, position: Vector2f, tpf: Float) extends Mapping with PositionMapping
case class AnalogMapping(name: String, value: Float, position: Vector2f, tpf: Float) extends Mapping with PositionMapping

case class RayMapping(ray: Ray, mousePos: Vector2f)

object ObservableFactory extends LazyLogging {

  def trace[T](msg: String = "", obs: Observable[T]) = {
    val tag = s"Obs-${obs.hashCode.toHexString.takeRight(4)}"
    logger.debug("{}: {} trace started", tag, msg)
    obs.subscribe(
      x => logger.debug("{}: {} onNext({})", tag, msg, x.toString),
      e => logger.error(s"$tag: $msg", e),
      () => logger.debug("{}: {} onComplete()", tag, msg))
  }

  /**
   * Creates an observable that emits just the last value of  a variable
   * since creation instant
   * The first value will be emitted with sampled observable the further values are immediate
   */
  def storeValueObs[T](a: Observable[T]): Observable[T] = {
    val subj = Subject[T]
    var value: Option[T] = None

    a.subscribe(
      x => {
        value = Option(x)
        subj.onNext(x)
      },
      ex => subj.onError(ex),
      () => subj.onCompleted)

    Observable.create(obsr => {
      val v = value
      if (v.isEmpty) {
        subj.take(1).subscribe(obsr)
      } else {
        Observable.just(v.get).subscribe(obsr)
      }
    })
  }

  /**
   * Creates an observable that emits the value created at
   * first emission of observable since first subscription
   */
  def onFirstObs[T, R](t: Observable[T])(f: T => R = (x: T) => x): Observable[R] = {
    val fo = for { t <- t.take(1) } yield f(t)
    val fc = storeValueObs(fo)
    fc
  }

  /**
   * Creates an observable that emits the values
   * first observable emitted by observable since first subscription
   */
  def onFirstFlattenObs[T, R](t: Observable[T])(f: T => Observable[R]): Observable[R] =
    onFirstObs(t)(f).flatten

  /**
   * Creates an observable that emits the value created when
   * first two trigger observables will be emitted since first subscription
   */
  def onFirstObs[T, V, R](
    t: Observable[T],
    v: Observable[V])(
      f: (T, V) => R): Observable[R] = {

    val fo = (for {
      t <- t.take(1)
    } yield {
      val r = for { va <- v.take(1) } yield f(t, va)
      val rc = storeValueObs(r)
      trace("rc", rc)
      rc
    })
    val fc = storeValueObs(fo)
    fc.flatten
  }

  /**
   * Creates an observable that emits the value composed by trigger observable and
   * previous value of sampling observable
   * or optional default values if no previous value available
   */
  def trigger[T, S, R](trigger: Observable[T], value: Observable[S])(f: (T, S) => R = (t: T, s: S) => (t, s))(init: Option[S] = None): Observable[R] = {
    val storeObs = storeValueObs(
      if (init.isEmpty) { value } else { Observable.just(init.get) merge value })
    val x = for { t <- trigger } yield for { v <- storeObs } yield f(t, v)
    x.flatten
  }

  /** State flow */
  def stateFlow[T](init: T)(f: Observable[T => T]): Observable[T] = {
    val subj = Subject[T]
    var acc: T = init
    var count = 0
    var sub: Option[Subscription] = None

    Observable.create[T] { observer =>
      {
        count = count + 1
        if (sub.isEmpty) {
          sub = Some(f.subscribe(
            (tx: T => T) => {
              acc = tx(acc)
              subj.onNext(acc)
            },
            e => subj.onError(e),
            subj.onCompleted))
        }

        val txSub = subj.subscribe(
          t => observer.onNext(t),
          e => observer.onError(e),
          observer.onCompleted)

        Subscription {
          count = count - 1
          if (count == 0) {
            for (s <- sub) s.unsubscribe
            sub = None
          }
          txSub.unsubscribe()
        }
      }
    }
  }

  /** Creates an observable that emits a sequence of last n values emitted */
  def history[T](value: Observable[T])(length: Int): Observable[Seq[T]] =
    value.scan(Seq[T]())((seq, current) => current +: seq.take(length - 1))

  /** Returns the observable of pick ray */
  //  def pickRay(o: Observable[PositionMapping]): Observable[RayMapping] =
  //    for { _ <- o } yield {
  //      val mousePos = app.getInputManager.getCursorPosition
  //      val cam = app.getCamera
  //      val pos = cam.getWorldCoordinates(new Vector2f(mousePos), 0f).clone()
  //      val dir = cam.getWorldCoordinates(new Vector2f(mousePos), 1f).subtractLocal(pos).normalizeLocal()
  //      RayMapping(new Ray(pos, dir), new Vector2f(mousePos))
  //    }
  //
  //  /** Returns the observable of pick object */
  //  def pickCollision(shootables: Node)(o: Observable[RayMapping]): Observable[(CollisionResult, RayMapping)] = {
  //    val collisions = for { rayMapping <- o } yield {
  //      val results = new CollisionResults()
  //      shootables.collideWith(rayMapping.ray, results)
  //      (results, rayMapping)
  //    }
  //    for {
  //      (cr, ray) <- collisions
  //      if (cr.size() > 0)
  //    } yield (cr.getClosestCollision(), ray)
  //  }

}