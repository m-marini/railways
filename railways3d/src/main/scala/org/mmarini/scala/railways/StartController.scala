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
import de.lessvoid.nifty.controls.ButtonClickedEvent
import de.lessvoid.nifty.NiftyEventSubscriber
import org.mmarini.scala.jmonkey.SelectionObservable
import org.mmarini.scala.jmonkey.DefaultScreenController

/**
 * @author us00852
 *
 */
class StartController extends AbstractAppState
    with DefaultScreenController
    with SelectionObservable with LazyLogging {

  /** Returns the station element renderer */
  private def station = redererById("station", classOf[TextRenderer])

  /** Returns the level element renderer */
  private def level = redererById("level", classOf[TextRenderer])

  /** Returns the duration element renderer */
  private def duration = redererById("duration", classOf[TextRenderer])

  /** Returns the game parameter observer */
  def show(parms: GameParameters) {
    station.foreach(_.setText(parms.stationName))
    level.foreach(_.setText(parms.levelName))
    duration.foreach(_.setText(parms.durationName))
  }
}
