package org.mmarini.railways3d

import com.jme3.app.SimpleApplication
import com.jme3.niftygui.NiftyJmeDisplay

object Main extends SimpleApplication {

  /**
   *
   */
  @Override
  def simpleInitApp: Unit = {
    val niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort)

    // Create a new NiftyGUI object
    val nifty = niftyDisplay.getNifty()

    // Read your XML and initialize your custom ScreenController
    nifty.fromXml("Interface/screen.xml", "start")

    // attach the Nifty display to the gui view port as a processor
    guiViewPort.addProcessor(niftyDisplay)

    // disable the fly cam
    flyCam.setDragToRotate(true)
  }

  /**
   *
   */
  def main(args: Array[String]): Unit = {
    Main.start()
  }
}