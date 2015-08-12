/**
 *
 */
package org.mmarini.scala.jmonkey

import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subscription
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEvent
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent
import collection.JavaConversions._
import de.lessvoid.nifty.controls.ButtonClickedEvent

/**
 *
 */
class ListBoxSelectionObservable[T](obs: Observable[ListBoxSelectionChangedEvent[T]]) {

  /** Returns an observable of selection list */
  def selections: Observable[IndexedSeq[T]] = for { e <- obs } yield e.getSelection.toIndexedSeq

  /** Returns an observable of single option selection */
  def singleSelection: Observable[Option[T]] = for { e <- obs } yield {
    val l = e.getSelection
    if (l.isEmpty()) None else Some(l.head)
  }
}
