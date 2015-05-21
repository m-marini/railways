/**
 *
 */
package org.mmarini.scala.railways

import scala.util.Try

import org.mmarini.scala.railways.model.Block
import org.mmarini.scala.railways.model.BlockStatus
import org.mmarini.scala.railways.model.BlockTemplate
import org.mmarini.scala.railways.model.Entry
import org.mmarini.scala.railways.model.EntryStatus
import org.mmarini.scala.railways.model.Exit
import org.mmarini.scala.railways.model.ExitStatus
import org.mmarini.scala.railways.model.GameParameters
import org.mmarini.scala.railways.model.GameStatus
import org.mmarini.scala.railways.model.Platform
import org.mmarini.scala.railways.model.PlatformStatus

import com.jme3.asset.AssetManager
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.math.ColorRGBA
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.util.SkyFactory
import com.typesafe.scalalogging.LazyLogging

import rx.lang.scala.Observer

/**
 * Handles the events of simulation coming from user or clock ticks
 *
 * Each event generates a change of rendered model 3d.
 * The model is kept in this Game
 *
 * This Game constructor initializes the game status
 */
class Game(app: Main.type, parameters: GameParameters) extends LazyLogging {

  private val RedSemModel = "Textures/blocks/red-sem.j3o"
  private val GreenSemModel = "Textures/blocks/green-sem.j3o"
  private val RedPlatModel = "Textures/blocks/red-plat.j3o"
  private val GreenPlatModel = "Textures/blocks/green-plat.j3o"

  /** Returns all the spatial names of a block template */
  private val StatusKeys: Map[BlockTemplate, Set[String]] = Map(
    Entry -> Set(RedSemModel),
    Exit -> Set(
      RedSemModel,
      GreenSemModel),
    Platform -> Set(
      RedPlatModel,
      GreenPlatModel))

  loadBackstage

  try {
    app.getCamera.getLocation().setX(200f)
    app.getCamera.getLocation().setY(1.7f + 0.5f)
    app.getCamera.getLocation().setZ(10f)
    //    app.getCamera.setAxes(new Quaternion().fromAngleAxis(-Pif / 2, new Vector3f(0f, 1f, 0f)))
  } catch { case e: Throwable => logger.error(e.getMessage, e) }

  /** Returns the initial game status */
  private val initialStatus = GameStatus(parameters)

  /** Returns the [[BlockStatus]] observers that change the scene */
  private val blocksObservers = createBlockObservers(
    app.getAssetManager,
    initialStatus.blocks.values.map(_.block).toSet)

  /** events observable */
  private val events = app.timeObservable.map[GameStatus => GameStatus](
    p => s => s.tick(p._2))

  /** game state observable */
  private val state = stateFlow(initialStatus)(events)

  state.subscribe(createBlockObservers(
    app.getAssetManager,
    initialStatus.blocks.values.map(_.block).toSet))

  /** Loads backstage of scene */
  private def loadBackstage {
    // Load sky
    val rootNode = app.getRootNode
    try {

      object SkyboxIndex extends Enumeration {
        val East, West, North, South, Up, Down = Value
      }

      import SkyboxIndex._
      implicit def skyboxIndexToInt(idx: SkyboxIndex.Value): Int = idx.id

      val assetManager = app.getAssetManager
      val imgs = (for (idx <- SkyboxIndex.values) yield {
        val tex = assetManager.loadTexture(s"Textures/sky/ref-sky_${idx.toString.toLowerCase}.png")
        (idx -> tex)
      }).toMap

      val sky = SkyFactory.createSky(assetManager, imgs(East), imgs(West), imgs(North), imgs(South), imgs(Up), imgs(Down))

      rootNode.attachChild(sky)
    } catch { case e: Throwable => logger.error(e.getMessage, e) }

    // Create ambient light
    try {
      val ambLight = new AmbientLight
      ambLight.setColor(ColorRGBA.White.mult(1.3f))
      rootNode.addLight(ambLight)
    } catch { case e: Throwable => logger.error(e.getMessage, e) }

    // Create sun light
    try {
      val sunLight = new DirectionalLight
      sunLight.setColor(ColorRGBA.White.mult(1.3f));
      sunLight.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal())
      rootNode.addLight(sunLight)
    } catch { case e: Throwable => logger.error(e.getMessage, e) }
  }

  /** Load 3d model of blocks */
  private def loadBlockModel(assetManager: AssetManager, blocks: Set[Block]) = (for {
    block <- blocks // For each block
  } yield {
    val spatialTrys =
      for (statKey <- StatusKeys(block.template)) // for each status of block load spatials
        yield Try {
        (statKey, {
          val spat = assetManager.loadModel(statKey)
          spat.setLocalRotation(new Quaternion().fromAngleAxis(block.rotAngle, new Vector3f(0f, -1f, 0f)))
          spat.setLocalTranslation(new Vector3f(block.x, 0f, block.y))
          spat
        })
      }
    // Dump loading errors
    spatialTrys.foreach { s => s.failed.foreach(ex => logger.error(ex.getMessage, ex)) }

    (block.id -> spatialTrys.filter(_.isSuccess).map(_.get).toMap)
  }).toMap

  /** Returns a factory function that builds Spatial for the BlockStatus */
  def createBlockObservers(assetManager: AssetManager, blocks: Set[Block]): Observer[GameStatus] = {

    // Create observer
    val cache = loadBlockModel(assetManager, blocks)
    val rootNode = app.getRootNode

    Observer((status: GameStatus) => {
      for (blockStatus <- status.blocks.values) {
        val key = statusKey(blockStatus)
        cache.getOrElse(blockStatus.block.id, Set()).
          foreach {
            case (k, spatial) if (k == key && !Option(spatial.getUserData[String]("attached")).contains(true)) =>
              rootNode.attachChild(spatial)
              spatial.setUserData("attached", true)
            case (k, spatial) if (k != key && Option(spatial.getUserData[String]("attached")).contains(true)) =>
              rootNode.detachChild(spatial)
              spatial.setUserData("attached", false)
            case _ =>
          }
      }
    })
  }

  /** Returns the status key of a block */
  def statusKey(status: BlockStatus): String = status match {
    case EntryStatus(_) => RedSemModel
    case ExitStatus(_, false) => GreenSemModel
    case ExitStatus(_, true) => RedSemModel
    case PlatformStatus(_, false) => GreenPlatModel
    case PlatformStatus(_, true) => RedPlatModel
  }

}
