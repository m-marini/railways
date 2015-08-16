/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.GameParameters
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.ListBox
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent
import de.lessvoid.nifty.screen.Screen
import rx.lang.scala.Observer
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEventSubscriber
import scala.util.Try
import de.lessvoid.nifty.elements.Element
import com.jme3.math.Vector2f
import org.mmarini.scala.jmonkey.ScreenObservable
import org.mmarini.scala.jmonkey.AbstractScreenController
import org.mmarini.scala.jmonkey.MousePrimarClickedObservable
import org.mmarini.scala.jmonkey.MousePrimarReleaseObservable
import org.mmarini.scala.jmonkey.ListBoxSelectionChangedObservable
import rx.lang.scala.Observable

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class GameController extends AbstractAppState
    with AbstractScreenController
    with ScreenObservable
    with MousePrimarClickedObservable
    with MousePrimarReleaseObservable
    with ListBoxSelectionChangedObservable[String]
    with LazyLogging {

  /** the observable of camera event selection */
  lazy private val _camerasEventObservable = Subject[ListBoxSelectionChangedEvent[String]]()

  /** the observable of camera selection */
  lazy val cameraSelectedObs = singleSelection(listBoxSelectionChangedObs)

  lazy val trainPopupOpt = createPopup("trainPopup")

  lazy val semPopupOpt = createPopup("semPopup")

  /** Shows the camera views */
  def show(cams: List[String]) {
    for {
      c <- controlById("cameras", classOf[ListBox[String]])
      item <- cams
    } c.addItem(item)
  }

  def showSemaphorePopup(pos: Vector2f) {
    for (s <- semPopupOpt)
      showPopupAt(s, "semPane", pos)
  }

  def showTrainPopup(pos: Vector2f) {
    for (s <- trainPopupOpt)
      showPopupAt(s, "trainPane", pos)
  }
}
