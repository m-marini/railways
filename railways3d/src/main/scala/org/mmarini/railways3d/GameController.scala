/**
 *
 */
package org.mmarini.railways3d

import org.mmarini.railways3d.model.GameParameters

import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.util.SkyFactory
import com.typesafe.scalalogging.LazyLogging

import rx.lang.scala.Observer

/**
 * @author us00852
 *
 */
class GameController extends AbstractAppState with AbstractController with LazyLogging {

  val gameStarterObserver = Observer((o: (Main.type, GameParameters)) => {
    o match {
      case (app, parms) =>
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

        import scala.math.sin

        val state =
          stateFlow(0f)(app.timeObservable.map(p => (x => x + p._2 * 10)))

        state.subscribe(x => coatch.setLocalTranslation(100f * sin(x / 10).toFloat, 0f, 0f))

    }
  })
}
