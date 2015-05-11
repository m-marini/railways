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
import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.jme3.scene.shape.Box
import com.jme3.math.ColorRGBA
import com.jme3.asset.AssetManager
import com.jme3.app.SimpleApplication
import com.jme3.util.SkyFactory
import rx.lang.scala.Observer

/**
 * @author us00852
 *
 */
class GameController extends AbstractAppState with AbstractController with LazyLogging {

  val gameStarter = Observer((o: (SimpleApplication, GameHandler)) => {
    o match {
      case (app, handler) =>
        val assetManager = app.getAssetManager
        val coatch = assetManager.loadModel("Models/veichles/coatch.j3o")
        val mat = new Material(app.getAssetManager, "Common/MatDefs/Misc/Unshaded.j3md")
        mat.setColor("Color", ColorRGBA.Blue)
        coatch.setMaterial(mat)
        val rootNode = app.getRootNode
        rootNode.attachChild(coatch)
        val west = assetManager.loadTexture("Textures/sky/lagoon_west.jpg")
        val east = assetManager.loadTexture("Textures/sky/lagoon_east.jpg")
        val north = assetManager.loadTexture("Textures/sky/lagoon_north.jpg")
        val south = assetManager.loadTexture("Textures/sky/lagoon_south.jpg")
        val up = assetManager.loadTexture("Textures/sky/lagoon_up.jpg")
        val down = assetManager.loadTexture("Textures/sky/lagoon_down.jpg")

        val sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down)
        rootNode.attachChild(sky)
    }
  })
}
