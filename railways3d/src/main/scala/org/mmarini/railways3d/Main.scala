/**
 *
 */
package org.mmarini.railways3d

import com.jme3.app.SimpleApplication
import com.jme3.niftygui.NiftyJmeDisplay
import org.mmarini.railways3d.model.GameHandler
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.ButtonClickedEvent
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.elements.events.ElementShowEvent
import de.lessvoid.nifty.NiftyDefaults
import org.mmarini.railways3d.model.GameParameters
import de.lessvoid.nifty.screen.ScreenController
import org.mmarini.railways3d.model.GameParameters
import org.mmarini.railways3d.model.GameParameters

/**
 *
 */
object Main extends SimpleApplication with LazyLogging {

  private var niftyDisplay: Option[NiftyJmeDisplay] = None

  /**
   *
   */
  @Override
  def simpleInitApp: Unit = {
    setDisplayStatView(false)
    setDisplayFps(false)
    niftyDisplay = Some(new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort))

    // Read your XML and initialize your custom ScreenController
    nifty.foreach(n => {
      n.fromXml("Interface/start.xml", "start")
      n.addXml("Interface/opts.xml")
      n.addXml("Interface/game.xml")
    })
    // attach the Nifty display to the gui view port as a processor
    niftyDisplay.foreach(guiViewPort.addProcessor(_))
    // disable the fly cam
    flyCam.setDragToRotate(true)
    for {
      start <- niftyController("start")
    } {
      start.asInstanceOf[StartController].applyHandler(
        new GameHandler(GameParameters(
          "Delta Crossing",
          "Easy",
          "Short",
          OptionsController.DurationEnum.valueById(OptionsController.DurationEnum.Short.id),
          OptionsController.FrequenceEnum.valueById(OptionsController.FrequenceEnum.Easy.id),
          true,
          false,
          OptionsController.DefaultVolume)))
    }
  }

  /**
   *
   */
  private def nifty = {
    niftyDisplay.map(n => n.getNifty())
  }

  /**
   *
   */
  private def niftyScreen(id: String) = {
    nifty.map(n => n.getScreen(id))
  }

  /**
   *
   */
  private def niftyController(id: String) = {
    niftyScreen(id).map(n => n.getScreenController())
  }

  /**
   *
   */
  def main(args: Array[String]): Unit = {
    Main.start()
  }

  /**
   *
   */
  def optionChanged(p: GameParameters) {
    logger.debug(s"optionChanged $p")
    niftyController("start").foreach(co => co.asInstanceOf[StartController].applyHandler(new GameHandler(p)))
    nifty.foreach(n => n.gotoScreen("start"))
  }

  /**
   *
   */
  def startGame(handler: GameHandler) {
    logger.debug(s"starting game $handler")
    nifty.foreach(n => n.gotoScreen("game-screen"))
    niftyController("game-screen").foreach(_.asInstanceOf[GameController].startGame(this,handler))
  }
}
