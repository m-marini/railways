/**
 *
 */
package org.mmarini

/**
 * @author us00852
 *
 */
import rx.lang.scala.Observable
import rx.lang.scala.Subject

package object railways3d {

  /**
   *
   */
  def sampled[T, S](trigger: Observable[T], value: Observable[S]): Observable[(T, S)] = {
    val r = Subject[(T, S)]()
    var v: Option[S] = None
    value.subscribe((s) => v = Some(s))
    trigger.subscribe(
      (t) => if (!v.isEmpty) r.onNext((t, v.get)),
      (ex) => r.onError(ex),
      r.onCompleted)
    r
  }

  /**
   *
   */
  def fold[E, S](f: ((E, S)) => S)(s0: S)(e: Observable[E]): Observable[S] = {
    val s = Subject[S]
    sampled(e, s).
      map(f).subscribe(
        x => s.onNext(x),
        ex => s.onError(ex),
        s.onCompleted)
    s.onNext(s0)
    s
  }
}