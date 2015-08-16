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

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class GameController extends AbstractAppState
    with AbstractScreenController
    with ScreenObservable
    with LazyLogging {

  /** the observable of camera event selection */
  lazy private val _camerasEventObservable = Subject[ListBoxSelectionChangedEvent[String]]()

  /** the observable of camera selection */
  lazy val cameraSelected = for { s <- _camerasEventObservable.singleSelection if (!s.isEmpty) } yield s.get

  /** Converts the camera events into camera event observer */
  @NiftyEventSubscriber(id = "cameras")
  def onListBoxSelectionChanged(id: String, event: ListBoxSelectionChangedEvent[String]) {
    _camerasEventObservable.onNext(event)
  }

  // 
  lazy val trainPopupOpt = createPopup("trainPopup")

  lazy val semPopupOpt = createPopup("semPopup")

  val buttonsObservable: Subject[(String, Boolean)] = Subject()

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

  /** */
  def onButton(btn: String) {
    buttonsObservable.onNext(btn, true)
  }

  /** */
  def onRelease(btn: String) {
    buttonsObservable.onNext(btn, false)
  }
}
