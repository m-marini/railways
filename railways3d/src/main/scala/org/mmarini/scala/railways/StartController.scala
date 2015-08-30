/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.ButtonClickedObservable
import org.mmarini.scala.railways.model.GameParameters
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.elements.render.TextRenderer
import org.mmarini.scala.jmonkey.ScreenControllerAdapter
import org.mmarini.scala.jmonkey.ScreenObservables
import org.mmarini.scala.railways.model.GameParameters
import rx.lang.scala.Subscription
import rx.lang.scala.Observable
import org.mmarini.scala.jmonkey.ScreenObservables
import rx.lang.scala.subscriptions.CompositeSubscription
import org.mmarini.scala.jmonkey.MousePrimaryClickedObservable

/**
 * @author us00852
 *
 */
class StartController extends ScreenControllerAdapter
    with ScreenObservables
    with ButtonClickedObservable
    with MousePrimaryClickedObservable
    with LazyLogging {

  /** Returns the game parameter observer */
  def show(parms: GameParameters) {
    redererByIdObs("station", classOf[TextRenderer]).subscribe(r => r.setText(parms.stationName))
    redererByIdObs("level", classOf[TextRenderer]).subscribe(r => r.setText(parms.levelName))
    redererByIdObs("duration", classOf[TextRenderer]).subscribe(r => r.setText(parms.durationName))
  }
}
