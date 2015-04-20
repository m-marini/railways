/**
 *
 */
package org.mmarini.railways3d

import com.jme3.app.SimpleApplication
import com.jme3.niftygui.NiftyJmeDisplay
import org.mmarini.railways3d.model.GameHandler
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.ButtonClickedEvent

/**
 *
 */
object Main extends SimpleApplication {

  /**
   *
   */
  @Override
  def simpleInitApp: Unit = {
    setDisplayStatView(false)
    setDisplayFps(false)
    val niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort)

    // Create a new NiftyGUI object
    val nifty = niftyDisplay.getNifty()

    // Read your XML and initialize your custom ScreenController
    nifty.fromXml("Interface/start.xml", "start")
    nifty.addXml("Interface/opts.xml");

    wire(nifty)

    // attach the Nifty display to the gui view port as a processor
    guiViewPort.addProcessor(niftyDisplay)

    // disable the fly cam
    flyCam.setDragToRotate(true)
  }

  /**
   *
   */
  private def wire(nifty: Nifty) = {
    val startScreen = nifty.getScreen("start")
    val startController = startScreen.getScreenController().asInstanceOf[StartScreen]

    val optsScreen = nifty.getScreen("opts-screen");
    val optsController = optsScreen.getScreenController().asInstanceOf[OptionsScreen]

    JME3Utils.subscribe(nifty, optsScreen, "ok", classOf[ButtonClickedEvent], (id: String, ev: ButtonClickedEvent) => {
      println("OK")
      val handler = new GameHandler(optsController.parameters)
      startController.applyHandler(handler)
      nifty.gotoScreen("start")
    })

    JME3Utils.subscribe(nifty, startScreen, "optionsButton", classOf[ButtonClickedEvent], (id: String, ev: ButtonClickedEvent) => {
      println("OK")
      nifty.gotoScreen("opts-screen")
    })

    this
  }

  /**
   *
   */
  def main(args: Array[String]): Unit = {
    Main.start()
  }
}
