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
import org.mmarini.scala.railways.model.TrainMessage
import org.mmarini.scala.railways.model.Train

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

  /** the observable of camera selection */
  lazy val cameraSelectedObs = singleSelection(listBoxSelectionChangedObs)

  lazy val trainPopupOpt = createPopup("trainPopup")

  lazy val semPopupOpt = createPopup("semPopup")

  private def logListOpt = controlById("messagesList", classOf[ListBox[String]])

  private def trainListOpt = controlById("trainsList", classOf[ListBox[String]])

  /** Shows the camera views in the camera list panel */
  def showCameras(cams: List[String]) {
    for {
      c <- controlById("camerasList", classOf[ListBox[String]])
      item <- cams
    } c.addItem(item)
  }

  /** Shows the messages in the messages list panel */
  def showMsgs(msgs: Seq[TrainMessage]) {
    for {
      c <- logListOpt
      item <- msgs
    } c.addItem(item.toString())
  }

  /** Shows the trains in the trains list panel */
  def showTrains(trains: Set[Train]) {
    for {
      ctrl <- trainListOpt
    } {
      ctrl.clear
      for {
        train <- trains
      } {
        val item = f"${train.id}%s ${(train.speed * 3.6).toInt}%d Km/h"
        ctrl.addItem(item)
      }
    }
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
