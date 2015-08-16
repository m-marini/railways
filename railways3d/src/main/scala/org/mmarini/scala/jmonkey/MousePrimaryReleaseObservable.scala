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
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryReleaseEvent

/**
 * @author us00852
 *
 */
trait MousePrimarReleaseObservable {

  private val mousePrimaryReleaseSubj = Subject[NiftyMousePrimaryReleaseEvent]()

  /** Return the selection button id observable */
  def mousePrimaryReleaseObs: Observable[NiftyMousePrimaryReleaseEvent] = mousePrimaryReleaseSubj

  @NiftyEventSubscriber(pattern = ".*")
  def select(id: String, event: NiftyMousePrimaryReleaseEvent) {
    mousePrimaryReleaseSubj.onNext(event)
  }
}
