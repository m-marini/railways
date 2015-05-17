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
        logger.info("starting game ...")
        val assetManager = app.getAssetManager

        val coach = assetManager.loadModel("Models/veichles/coach.j3o")
        val mat = new Material(app.getAssetManager, "Common/MatDefs/Misc/Unshaded.j3md")
        mat.setColor("Color", ColorRGBA.Gray)

        logger.info("loading platform ...")
        val platform = assetManager.loadModel("Models/blocks/green_plat.j3o")
        logger.info("Platform loaded ...")
        val mat1 = new Material(app.getAssetManager, "Common/MatDefs/Misc/Unshaded.j3md")
        mat1.setColor("Color", ColorRGBA.Green)
        platform.setMaterial(mat1)

        val rootNode = app.getRootNode
        rootNode.attachChild(platform)
        rootNode.attachChild(coach)
        logger.info("Platform attached ...")

        val name = "sky"
        val west = assetManager.loadTexture(s"Textures/sky/${name}_west.png")
        val east = assetManager.loadTexture(s"Textures/sky/${name}_east.png")
        val north = assetManager.loadTexture(s"Textures/sky/${name}_north.png")
        val south = assetManager.loadTexture(s"Textures/sky/${name}_south.png")
        val up = assetManager.loadTexture(s"Textures/sky/${name}_up.png")
        val down = assetManager.loadTexture(s"Textures/sky/${name}_down.png")

        val sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down)

        rootNode.attachChild(sky)
        logger.info("Sky attached...")

        import scala.math.sin

        val state =
          stateFlow(0f)(app.timeObservable.map(p => (x => x + p._2 * 10)))

        state.subscribe {
          x => coach.setLocalTranslation(192.5f * sin(x / 10).toFloat, 0f, 0f)
        }
    }
  })
}
