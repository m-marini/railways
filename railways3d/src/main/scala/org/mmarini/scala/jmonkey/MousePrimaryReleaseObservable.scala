/**
 *
 */
package org.mmarini.scala.jmonkey

import com.typesafe.scalalogging.LazyLogging

import de.lessvoid.nifty.NiftyEventSubscriber
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryReleaseEvent
import rx.lang.scala.Observable
import rx.lang.scala.Subject

/**
 * @author us00852
 *
 */
trait MousePrimaryReleaseObservable extends LazyLogging {

  private val mousePrimaryReleaseSubj = Subject[NiftyMousePrimaryReleaseEvent]()

  /** Return the selection button id observable */
  def mousePrimaryReleaseObs: Observable[NiftyMousePrimaryReleaseEvent] = mousePrimaryReleaseSubj

  @NiftyEventSubscriber(pattern = ".*")
  def select(id: String, event: NiftyMousePrimaryReleaseEvent) {
    logger.debug("Mouse released on {}: {}", id, event)
    mousePrimaryReleaseSubj.onNext(event)
  }
}
