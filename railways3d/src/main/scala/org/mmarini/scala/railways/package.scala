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

/** */
package object railways {

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
