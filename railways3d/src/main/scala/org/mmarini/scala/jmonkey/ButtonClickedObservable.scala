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
trait ButtonClickedObservable extends LazyLogging {

  private val buttonClickedSubj = Subject[ButtonClickedEvent]()

  /** Return the selection button id observable */
  def buttonClickedObs: Observable[ButtonClickedEvent] = buttonClickedSubj

  /** Converts the buttons press event into button id observable */
  @NiftyEventSubscriber(pattern = ".*")
  def select(id: String, event: ButtonClickedEvent) {
    //    logger.debug("Button clicked at {}: {}", id, event)
    buttonClickedSubj.onNext(event)
  }
}
