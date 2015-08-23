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

/**
 * @author us00852
 *
 */
trait MousePrimaryClickedObservable extends LazyLogging {

  private val mousePrimaryClickedSubj = Subject[NiftyMousePrimaryClickedEvent]()

  /** Return the selection button id observable */
  def mousePrimaryClickedObs: Observable[NiftyMousePrimaryClickedEvent] = mousePrimaryClickedSubj

  @NiftyEventSubscriber(pattern = ".*")
  def select(id: String, event: NiftyMousePrimaryClickedEvent) {
    logger.debug("Mouse clicked on {}: {}", id, event)
    mousePrimaryClickedSubj.onNext(event)
  }
}
