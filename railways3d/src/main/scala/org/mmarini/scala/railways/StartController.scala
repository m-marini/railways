/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.GameParameters
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.elements.render.TextRenderer
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subject
import org.mmarini.scala.jmonkey.AbstractController
import de.lessvoid.nifty.controls.ButtonClickedEvent
import de.lessvoid.nifty.NiftyEventSubscriber

/**
 * @author us00852
 *
 */
class StartController extends AbstractAppState with AbstractController with LazyLogging {

  private val _selection = Subject[String]()

  /** Returns the station element renderer */
  private def station = redererById("station", classOf[TextRenderer])

  /** Returns the level element renderer */
  private def level = redererById("level", classOf[TextRenderer])

  /** Returns the duration element renderer */
  private def duration = redererById("duration", classOf[TextRenderer])

  /** Converts the buttons press event into button id observable */
  @NiftyEventSubscriber(pattern = ".*")
  def select(id: String, event: ButtonClickedEvent) {
    _selection.onNext(id)
  }

  /** Returns the game parameter observer */
  def show(parms: GameParameters) {
    station.foreach(_.setText(parms.stationName))
    level.foreach(_.setText(parms.levelName))
    duration.foreach(_.setText(parms.durationName))
  }

  /** Return the selection button id observable */
  def selection: Observable[String] = _selection
}
