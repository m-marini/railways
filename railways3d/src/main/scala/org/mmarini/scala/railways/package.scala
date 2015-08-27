/**
 *
 */
package org.mmarini.scala

/**
 * @author us00852
 *
 */
import rx.lang.scala.Observable
import rx.lang.scala.Subject
import rx.lang.scala.Subscription
import rx.lang.scala.Observer
import rx.lang.scala.subscriptions.CompositeSubscription
import com.jme3.app.Application
import com.jme3.input.controls.AnalogListener
import com.jme3.input.controls.ActionListener
import scala.util.Try
import com.jme3.input.controls.Trigger
import com.jme3.scene.Spatial
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.math.Ray
import com.jme3.scene.Node
import com.jme3.collision.CollisionResults
import com.jme3.collision.CollisionResult
import com.jme3.input.InputManager
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent
import com.typesafe.scalalogging.LazyLogging
import scala.util.Random
import org.slf4j.Marker

/** */
package object railways extends LazyLogging {

  implicit class ObservableFactory[T](subject: Observable[T]) extends LazyLogging {

    def hash(o: Any) = o.hashCode.toHexString.takeRight(4)

    /** Creates an observable that emits a sequence of last n values emitted */
    def history(length: Int): Observable[Seq[T]] =
      subject.scan(Seq[T]())(
        (seq, current) => {
          val tail = seq.take(length - 1)
          current +: tail
        })

    /**
     * Creates an observable that emits just the last value of  a variable
     * since creation instant
     * The first value will be emitted with sampled observable the further values are immediate
     */
    def latest: Observable[T] = {
      var value: Observable[T] = subject.take(1)
      subject.subscribe(x => value = Observable.just(x),
        ex => value = Observable.error(ex),
        () => {})
      Observable.defer(value)
    }

    def traced(id: String): Observable[T] = {
      var ct = 0
      Observable.create[T](obsr => {
        ct = ct + 1
        val traceId = s"${hash(subject)}.$ct.${hash(obsr)} $id"
        logger.debug("{} subscribe", traceId)
        val sub = subject.subscribe(
          x => {
            logger.debug("{} onNext {}", traceId, String.valueOf(x))
            obsr.onNext(x)
          },
          e => {
            logger.error("$traceId error", traceId, e)
            obsr.onError(e)
          },
          () => {
            logger.debug("{}on Completed", traceId)
            obsr.onCompleted
          })
        Subscription {
          logger.debug("{} unsubscribe", traceId)
          sub.unsubscribe()
        }
      })
    }

    def trace(msg: String = "") = traced(msg).subscribe

    /**
     * Creates an observable that emits the value composed by trigger observable and
     * previous value of sampling observable
     * or optional default values if no previous value available
     */
    def withLatest[S, R](other: Observable[S])(implicit f: (T, S) => R = (t: T, s: S) => (t, s)): Observable[R] = {
      val latestSample = other.latest
      val x = for { t <- subject } yield for { v <- latestSample } yield f(t, v)
      x.flatten
    }
  }

  implicit class StateFlowFactory[T](subject: Observable[T => T]) {
    /** State flow */
    def statusFlow(init: Observable[T]): Observable[T] = {
      var s: Option[T] = None
      init.take(1).subscribe(x => { s = Option(x) })
      val subj = Subject[T]
      subject.dropUntil(init).subscribe(
        f => {
          // Computes new status
          val s1 = s.map(f)
          // Stores new status
          s = s1
          // emits new status
          s1.foreach(subj.onNext)
        },
        ex => subj.onError(ex),
        () => subj.onCompleted)
      subj
    }

    /** State flow */
    def statusFlow(init: T): Observable[T] =
      statusFlow(Observable.just(init))
  }

  implicit class OptionObservableFactory[T](subject: Observable[Option[T]]) {
    def optionFlatten: Observable[T] =
      subject.filterNot(_.isEmpty).map(_.get)
  }

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

  val OrientationAxis = Vector3f.UNIT_Y

  /** Shuffles a sequence */
  def shuffle[T](seq: IndexedSeq[T])(random: Random): IndexedSeq[T] = {
    val n = seq.length
    if (n <= 1) {
      seq
    } else {
      (0 until n - 1).foldLeft(seq)((seq, i) => {
        val j = random.nextInt(n - i) + i
        if (i != j) {
          seq.updated(j, seq(i)).updated(i, seq(j))
        } else {
          seq
        }
      })
    }
  }

  /** Returns an observable of single option selection */
  def singleSelection[T]: Observable[ListBoxSelectionChangedEvent[T]] => Observable[T] = (obs) =>
    for {
      e <- obs
      if (!e.getSelection.isEmpty)
    } yield e.getSelection.get(0)
}
