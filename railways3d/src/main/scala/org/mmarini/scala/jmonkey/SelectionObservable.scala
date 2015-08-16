/**
 *
 */
package org.mmarini.scala.jmonkey

import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.controls.ButtonClickedEvent
import rx.lang.scala.Observable
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEventSubscriber

/**
 * @author us00852
 *
 */
trait SelectionObservable {

  private val selectSubj = Subject[(String, ButtonClickedEvent)]()

  /** Return the selection button id observable */
  def selectObs: Observable[(String, ButtonClickedEvent)] = selectSubj

  /** Converts the buttons press event into button id observable */
  @NiftyEventSubscriber(pattern = ".*")
  def select(id: String, event: ButtonClickedEvent) {
    selectSubj.onNext((id, event))
  }
}
