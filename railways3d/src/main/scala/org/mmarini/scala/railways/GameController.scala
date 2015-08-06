/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.AbstractController
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

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class GameController extends AbstractAppState with AbstractController with LazyLogging {

  /** the observable of camera event selection */
  lazy private val _camerasEventObservable = Subject[ListBoxSelectionChangedEvent[String]]()

  /** the observable of camera selection */
  lazy val cameraSelected = _camerasEventObservable.singleSelection.filterNot(_.isEmpty)

  /** Converts the camera events into camera event observer */
  @NiftyEventSubscriber(id = "cameras")
  def onListBoxSelectionChanged(id: String, event: ListBoxSelectionChangedEvent[String]) {
    _camerasEventObservable.onNext(event)
  }

  lazy val popup = for (n <- nifty) yield {
    val popupTry = Try {
      n.createPopup("niftyPopupMenu")
    }
    for (ex <- popupTry.failed) {
      logger.error(ex.getMessage, ex)
    }
    popupTry.getOrElse(null)
  }

  /** Shows the camera views */
  def show(cams: List[String]) {
    for {
      c <- control("cameras", classOf[ListBox[String]])
      item <- cams
    } c.addItem(item)
  }
}
