package org.mmarini.railways3d

import com.jme3.app.SimpleApplication

object Main extends SimpleApplication {

  /**
   * 
   */
  @Override
  def simpleInitApp: Unit = {
    val spatial = assetManager.loadModel("Interface/coatch.j3o")
    rootNode.attachChild(spatial);
  }

  /**
   *
   */
  def main(args: Array[String]): Unit = {
    Main.start()
  }
}