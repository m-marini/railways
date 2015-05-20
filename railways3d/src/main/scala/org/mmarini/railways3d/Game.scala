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
import scala.util.Try

/**
 * Handles the events of simulation coming from user or clock ticks
 *
 * Each event generates a change of rendered model 3d.
 * The model is kept in this Game
 *
 * This Game constructor initializes the game status
 */
class Game(app: Main.type, parameters: GameParameters) extends LazyLogging {

  loadBackstage

  app.getCamera.getLocation().setX(0f)
  app.getCamera.getLocation().setY(1.7f + 0.5f)
  app.getCamera.getLocation().setZ(10f)
  //    app.getCamera.setAxes(new Quaternion().fromAngleAxis(-Pif / 2, new Vector3f(0f, 1f, 0f)))

  /** Returns the initial game status */
  private val initialStatus = GameStatus(parameters)
  logger.debug(s"initialStatus ${initialStatus.blocks}")

  /** Returns the factory of spatial */
  private val factory = BlockModel3d.factory(
    app.getAssetManager,
    initialStatus.blocks.values.map(_.block).toSet);

  /** Returns the initial block model */
  private val initialBlocksModel: Map[String, BlockModel3d] =
    initialStatus.blocks.map {
      case (id, status) => {
        logger.debug(s"Mapping $status")
        (id -> {
          val t = Try { BlockModel3d(status, app, factory) }
          t.failed.foreach(ex => logger.error(ex.getMessage(), ex))
          t
        })
      }
    }.
      filter(_._2.isSuccess).
      map {
        case (k, t) => (k -> t.get)
      }


  /** events observable */
  private val events = app.timeObservable.map[GameStatus => GameStatus](p => s => s.tick(p._2))

  /** game state observable */
  private val state = stateFlow(initialStatus)(events)

  /** the observable state flow of BlockModels */
  private val blockModels: Observable[Map[String, BlockModel3d]] = stateFlow(initialBlocksModel)(
    state.map(status => model =>
      updateBlockModel(status, model)))

  /** Returns the new model changing the root node of scene */
  def updateBlockModel(status: GameStatus, model: Map[String, BlockModel3d]): Map[String, BlockModel3d] =
    model.map {
      case (id, model) =>
        val blockStatus = status.blocks(id)
        (id, model(blockStatus)(app))
    }

  /** Loads backstage of scene */
  private def loadBackstage {
    val sky = Try {
      val assetManager = app.getAssetManager
      val imgs =
        for (s <- IndexedSeq("east", "west", "north", "south", "up", "down"))
          yield assetManager.loadTexture(s"Textures/sky/ref-sky_$s.png")
      SkyFactory.createSky(assetManager, imgs(0), imgs(1), imgs(2), imgs(3), imgs(4), imgs(5))
    }
    sky.failed.foreach(ex => logger.error(ex.getMessage(), ex))

    val rootNode = app.getRootNode
    sky.foreach(rootNode.attachChild)

    val ambLight = new AmbientLight
    ambLight.setColor(ColorRGBA.White.mult(1.3f))
    rootNode.addLight(ambLight)

    val sunLight = new DirectionalLight
    sunLight.setColor(ColorRGBA.White.mult(1.3f));
    sunLight.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal())
    rootNode.addLight(sunLight)
  }

  /** Returns the texture load */
}