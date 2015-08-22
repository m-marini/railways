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
import org.mmarini.scala.jmonkey.ApplicationOps
import org.mmarini.scala.jmonkey.InputManagerOps
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent
import com.typesafe.scalalogging.LazyLogging
import scala.util.Random

/** */
package object railways extends LazyLogging {

  val OrientationAxis = Vector3f.UNIT_Y

  implicit def appToAppOps(app: Application): ApplicationOps = new ApplicationOps(app)
  implicit def inpManagerToinpManagerOps(app: InputManager): InputManagerOps = new InputManagerOps(app)

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

  /**
   *
   */
  def trigger[T, S](trigger: Observable[T], value: Observable[S], init: Option[S] = None): Observable[(T, S)] = {

    val r = Observable.create((o: Observer[(T, S)]) => {

      var v: Option[S] = init

      val triggerSubscription = trigger.subscribe(
        (t) => if (!v.isEmpty) o.onNext((t, v.get)),
        (ex) => o.onError(ex),
        o.onCompleted)

      val valueSubscription = value.subscribe(
        (s) => v = Some(s),
        (ex) => {
          o.onError(ex)
          triggerSubscription.unsubscribe
          o.onCompleted
        },
        () => {})
      CompositeSubscription(valueSubscription, triggerSubscription)
    })
    r
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

  def history[T](value: Observable[T])(length: Int): Observable[Seq[T]] =
    value.scan(Seq[T]())((seq, current) => current +: seq.take(length - 1))

  def mergeAll[T](seq: Observable[T]*): Observable[T] =
    seq.reduce((a, b) => a merge b)
}
