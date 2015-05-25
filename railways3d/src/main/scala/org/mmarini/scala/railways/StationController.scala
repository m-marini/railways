/**
 *
 */
package org.mmarini.scala.railways

import scala.util.Try
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
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.terrain.geomipmap.TerrainLodControl
import com.jme3.terrain.geomipmap.TerrainQuad
import com.jme3.terrain.heightmap.ImageBasedHeightMap
import com.jme3.texture.Texture.WrapMode
import com.jme3.util.SkyFactory
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Observer
import com.jme3.collision.CollisionResult
import rx.lang.scala.Subscription
import com.jme3.input.controls.MouseAxisTrigger
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.input.MouseInput
import com.jme3.cinematic.events.MotionEvent
import com.jme3.animation.LoopMode
import com.jme3.cinematic.MotionPath
import com.jme3.scene.CameraNode
import com.jme3.scene.control.CameraControl.ControlDirection
import com.jme3.input.ChaseCamera
import scala.util.Random
import scala.collection.immutable.Vector
import rx.lang.scala.Observable
import com.jme3.scene.Spatial
import com.jme3.scene.control.CameraControl
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.KeyInput
import org.mmarini.scala.railways.model.Block
import com.jme3.asset.AssetManager
import com.jme3.scene.Node

/**
 * Handles the events of simulation coming from user or clock ticks
 *
 * Each event generates a change of rendered model 3d.
 * The model is kept in this Game
 *
 * This Game constructor initializes the game status
 */
class StationController(
  status: Observable[GameStatus],
  initialStatus: GameStatus,
  assetManager: AssetManager,
  rootNode: Node) extends LazyLogging {

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

  private val blocks = initialStatus.blocks.values.map(_.block).toSet

  /** Returns the [[BlockStatus]] observers that change the scene */
  private val blocksObservers = createBlocksObserver

  /** Returns the subscription that manages the station 3d model changes */
  def subscribe: Subscription = status.subscribe(blocksObservers)

  /** Load 3d model of blocks */
  private def loadBlockModel: Map[String, Map[String, Spatial]] = {
    val assets = for {
      // For each block
      block <- blocks
    } yield {
      val spatialTrys =
        for { statKey <- StatusKeys(block.template) } // for each status of block load spatials
          yield Try {
          val spat = assetManager.loadModel(statKey)
          spat.setLocalRotation(new Quaternion().fromAngleAxis(block.rotAngle, new Vector3f(0f, -1f, 0f)))
          spat.setLocalTranslation(new Vector3f(block.x, 0f, block.y))
          (statKey, spat)
        }
      // Dump loading errors
      spatialTrys.foreach { s => s.failed.foreach(ex => logger.error(ex.getMessage, ex)) }
      val success = spatialTrys.filter(_.isSuccess).map(_.get).toMap
      // Filter success assets
      (block.id -> success)
    }
    assets.toMap
  }

  /** Returns a factory function that builds Spatial for the BlockStatus */
  private def createBlocksObserver: Observer[GameStatus] = {

    // Create observer
    val cache = loadBlockModel.withDefaultValue(Set())

    Observer((status: GameStatus) =>
      for { blockStatus <- status.blocks.values } {
        val key = statusKey(blockStatus)
        cache(blockStatus.block.id).foreach(changeSpatials)

        def changeSpatials(block: (String, Spatial)) = block match {
          case (k, spatial) if (k == key && !Option(spatial.getUserData[String]("attached")).contains(true)) =>
            rootNode.attachChild(spatial)
            spatial.setUserData("attached", true)
          case (k, spatial) if (k != key && Option(spatial.getUserData[String]("attached")).contains(true)) =>
            rootNode.detachChild(spatial)
            spatial.setUserData("attached", false)
          case _ =>
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
