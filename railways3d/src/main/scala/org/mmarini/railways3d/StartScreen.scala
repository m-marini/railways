/**
 *
 */
package org.mmarini.railways3d

import com.jme3.app.state.AbstractAppState
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import org.mmarini.railways3d.model.GameHandler
import de.lessvoid.nifty.elements.render.TextRenderer
import org.mmarini.railways3d.model.GameParameters
import scala.collection.parallel.mutable.ParMap
import org.mmarini.railways3d.model.GameParameters

/**
 * @author us00852
 *
 */
class StartScreen extends AbstractAppState with ScreenController {
  private var nifty: Option[Nifty] = None
  private var screen: Option[Screen] = None
  private var handler: Option[GameHandler] = None

  /**
   *
   */
  def bind(nifty: Nifty, screen: Screen) {
    this.nifty = Some(nifty)
    this.screen = Some(screen)
  }

  /**
   *
   */
  private def applyUI = {
    // Read data
    val (stationName, levelName, durationName) = handler match {
      case Some(h) => h.parms match {
        case GameParameters(stationName, levelName, durationName, _, _, _, _, _) =>
          (stationName, levelName, durationName)
      }
      case None => ("???", "???", "???")
    }

    /*
     * apply to UI
     */
    screen.map(s => {
      s.findElementByName("station").
        getRenderer(classOf[TextRenderer]).
        setText(stationName)
      s.findElementByName("level").
        getRenderer(classOf[TextRenderer]).
        setText(levelName)
      s.
        findElementByName("duration").
        getRenderer(classOf[TextRenderer]).
        setText(durationName)
    })
    this
  }

  /**
   *
   */
  def applyHandler(handler: GameHandler): StartScreen = {
    this.handler = Some(handler)
    applyUI
  }

  /**
   *
   */
  def onStartScreen() {
    applyUI
  }

  /**
   *
   */
  def onEndScreen() {
  }

  /**
   *
   */
  def gotoScreen(id: String) {
    nifty.map(_.gotoScreen(id))
  }

  /**
   *
   */
  def quitGame() {
    Main.stop()
  }
}
