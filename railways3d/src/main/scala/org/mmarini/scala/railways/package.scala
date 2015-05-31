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
import org.mmarini.scala.jmonkey.ListBoxSelectionObservable

/** */
package object railways {

  implicit def appToAppOps(app: Application): ApplicationOps = new ApplicationOps(app)
  implicit def inpManagerToinpManagerOps(app: InputManager): InputManagerOps = new InputManagerOps(app)
  implicit def toListBoxSelectionObservable[T](obs: Observable[ListBoxSelectionChangedEvent[T]]): ListBoxSelectionObservable[T] =
    new ListBoxSelectionObservable(obs)

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

  /**
   *
   */
  def stateFlow[S](s0: S)(transition: Observable[S => S]): Observable[S] =
    Observable.create((o: Observer[S]) => {
      o.onNext(s0)
      var s = s0
      transition.subscribe(
        f => {
          s = f(s)
          o.onNext(s)
        },
        ex => o.onError(ex),
        o.onCompleted)
    })
}
