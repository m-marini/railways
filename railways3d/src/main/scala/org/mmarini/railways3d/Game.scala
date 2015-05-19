package org.mmarini.railways3d

import org.mmarini.railways3d.model.GameParameters
import com.typesafe.scalalogging.LazyLogging
import com.jme3.light.DirectionalLight
import com.jme3.util.SkyFactory
import com.jme3.light.AmbientLight
import com.jme3.app.SimpleApplication
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Spatial
import org.mmarini.railways3d.model.GameStatus
import org.mmarini.railways3d.model.Topology
import scala.math.sin
import org.mmarini.railways3d.model.Downville
import org.mmarini.railways3d.model.GameStatus
import org.mmarini.railways3d.model.Block
import rx.lang.scala.Observable

/**
 * A Game handles the events of simulation coming from user or clock ticks
 *
 * Each event generates a change of rendered model 3d.
 * The model is kept in this Game
 *
 * The Game constructor loads all the jme3 model templates and
 * initializes the game status
 */
class Game(app: Main.type, parameters: GameParameters) extends LazyLogging {
  private val Templates = List(
    "Models/veichles/coach.j3o",
    "Models/blocks/green_plat.j3o",
    "Models/blocks/red_plat.j3o",
    "Models/blocks/entry.j3o",
    "Models/blocks/green_exit.j3o",
    "Models/blocks/red_exit.j3o")

  private val assetManager = app.getAssetManager

  /** station topology */
  private val station = Downville

  /** events observable */
  private val events = app.timeObservable.map[GameStatus => GameStatus](p => s => s.tick(p._2))

  /** game state observable */
  val state = stateFlow(initialStatus)(events)

  /** Returns the initial game status */
  private def initialStatus = GameStatus(parameters, Downville, 0f, Map())

  /** jme3d templates */
  private val jme3dTemplates: Map[String, Spatial] =
    (for (name <- Templates)
      yield (name -> assetManager.loadModel(name))).
      toMap

  /** scene root node */
  private val rootNode = app.getRootNode

  loadBackstage

  app.getCamera.getLocation().setX(0f)
  app.getCamera.getLocation().setY(1.7f + 0.5f)
  app.getCamera.getLocation().setZ(2f)

  type BlocksModel3d = Map[String, BlockModel3d]

  /** the observable state flow of BlockModels */
  private val blockModels: Observable[BlocksModel3d] = stateFlow(initialBlockModel)(
    state.map(status => model => updateBlockModel(status, model)))

  /** Returns the new model changing the root node of scene */
  def updateBlockModel(status: GameStatus, model: BlocksModel3d): BlocksModel3d =
    model.map {
      case (id, model) =>
        val blockStatus = status.blocks(id)
        (id, model(blockStatus)(rootNode))
    }

  /** Returns the initial block model */
  private def initialBlockModel: BlocksModel3d = initialStatus.blocks.map(
    ???)

  /** Loads backstage of scene */
  private def loadBackstage {
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
  }

}