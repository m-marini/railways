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
import de.lessvoid.nifty.controls.ButtonClickedEvent
import com.typesafe.scalalogging.LazyLogging

/**
 * @author us00852
 *
 */
class StartController extends AbstractAppState with AbstractController with LazyLogging {
  private var screen: Option[Screen] = None
  private var handler: Option[GameHandler] = None

  /**
   *
   */
  override def bind(nifty: Nifty, screen: Screen) {
    logger.debug("bind ...")
    super.bind(nifty, screen)
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
  def applyHandler(handler: GameHandler): StartController = {
    this.handler = Some(handler)
    applyUI
  }

  /**
   *
   */
  override def onStartScreen {
    applyUI
  }

  /**
   *
   */
  def startGame {
    handler.foreach(Main.startGame(_))
  }

  /**
   *
   */
  def quitGame {
    Main.stop()
  }
}
