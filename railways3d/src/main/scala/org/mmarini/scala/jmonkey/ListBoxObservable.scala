/**
 *
 */
package org.mmarini.scala.jmonkey

import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.controls.ButtonClickedEvent
import rx.lang.scala.Observable
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEventSubscriber
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryClickedEvent
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent

/**
 * @author us00852
 *
 */
trait ListBoxSelectionChangedObservable[T] extends LazyLogging {

  private val listBoxSelectionChangedSubj = Subject[ListBoxSelectionChangedEvent[T]]()

  /** Return the selection button id observable */
  def listBoxSelectionChangedObs: Observable[ListBoxSelectionChangedEvent[T]] = listBoxSelectionChangedSubj

  /** Converts the camera events into camera event observer */
  @NiftyEventSubscriber(pattern = ".*")
  def onListBoxSelectionChanged(id: String, event: ListBoxSelectionChangedEvent[T]) {
    logger.debug("List box selection {} changed {}", id, event)
    listBoxSelectionChangedSubj.onNext(event)
  }
}
