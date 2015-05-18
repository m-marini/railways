package org.mmarini.railways3d

import org.mmarini.railways3d.model.GameParameters
import com.typesafe.scalalogging.LazyLogging
import com.jme3.light.DirectionalLight
import com.jme3.util.SkyFactory
import com.jme3.light.AmbientLight
import com.jme3.app.SimpleApplication
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f

/**
 *
 */
class Game(app: Main.type, parameters: GameParameters) extends LazyLogging {

  val assetManager = app.getAssetManager

  val coach = assetManager.loadModel("Models/veichles/coach.j3o")

  logger.info("loading platform ...")
  val platform = assetManager.loadModel("Models/blocks/green_plat.j3o")
  logger.info("Platform loaded ...")

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

  val ambLight = new AmbientLight
  ambLight.setColor(ColorRGBA.White.mult(1.3f))
  rootNode.addLight(ambLight)

  val sunLight = new DirectionalLight
  sunLight.setColor(ColorRGBA.White.mult(1.3f));
  sunLight.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal())
  rootNode.addLight(sunLight)

  import scala.math.sin

  val state =
    stateFlow(0f)(app.timeObservable.map(p => (x => x + p._2 * 10)))

  state.subscribe {
    x => coach.setLocalTranslation(0f,0f,192.5f * sin(x / 20).toFloat)
  }

  app.getCamera.getLocation().setX(0f)
  app.getCamera.getLocation().setY(1.7f + 0.5f)
  app.getCamera.getLocation().setZ(2f)

}