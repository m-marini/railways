/**
 *
 */
package org.mmarini.railways3d

import com.jme3.scene.Spatial
import org.mmarini.railways3d.model.Block
import org.mmarini.railways3d.model.BlockStatus
import com.jme3.scene.Node
import org.mmarini.railways3d.model.BlockStatus
import com.jme3.app.SimpleApplication
import com.sun.org.apache.bcel.internal.generic.LoadClass
import org.mmarini.railways3d.model.BlockTemplate
import org.mmarini.railways3d.model.Entry
import org.mmarini.railways3d.model.Exit
import org.mmarini.railways3d.model.Platform
import com.jme3.asset.AssetManager
import com.jme3.math.Vector3f
import com.jme3.math.Quaternion
import org.mmarini.railways3d.model.ExitStatus
import org.mmarini.railways3d.model.PlatformStatus
import org.mmarini.railways3d.model.EntryStatus
import com.typesafe.scalalogging.LazyLogging
import scala.util.control.NonFatal
import scala.util.Success
import scala.util.Failure
import scala.util.Try

/**
 * A 3d block model contains the current jme3 spatial of the block
 * and the spatial factory for different states
 *
 * Generates new BlockModel applying a status to the model and updating the rootNode of scene
 */
case class BlockModel3d(spatial: Spatial, factory: (BlockStatus => Spatial)) extends LazyLogging {

  /** Returns the next 3d block model appropriate to the game block status */
  def apply(block: BlockStatus)(app: SimpleApplication): BlockModel3d = {
    val ns = factory(block)
    if (spatial != ns) {
      try {
        app.getRootNode.detachChild(spatial)
        app.getRootNode.attachChild(ns)
      } catch { case e: Throwable => logger.error(e.getMessage(), e) }
      BlockModel3d(ns, factory)
    } else {
      this
    }
  }
}

/** A factory of [[BlockModel3d]] */
object BlockModel3d extends LazyLogging {

  private val RedSemModel = "Textures/blocks/red-sem.j3o"
  private val GreenSemModel = "Textures/blocks/green-sem.j3o"
  private val RedPlatModel = "Textures/blocks/red-plat.j3o"
  private val GreenPlatModel = "Textures/blocks/green-plat.j3o"

  /** Returns the [[BlockModel3d]] for the specific block */
  def apply(status: BlockStatus, app: SimpleApplication, factory: (BlockStatus) => Spatial): BlockModel3d = {
    val spatial = factory(status)
    try {
      app.getRootNode.attachChild(spatial)
    } catch { case e: Throwable => logger.error(e.getMessage(), e) }
    BlockModel3d(spatial, factory)
  }

  type BlockModelId = (String, String)

  /** Returns all the spatial names of a block template */
  private val statusKeys: Map[BlockTemplate, Set[String]] = Map(
    Entry -> Set(RedSemModel),
    Exit -> Set(
      RedSemModel,
      GreenSemModel),
    Platform -> Set(
      RedPlatModel,
      GreenPlatModel))

  /** Returns the quaternion of rotation round Y axis */
  private def rotation(angle: Float) =
    new Quaternion().fromAngleAxis(angle, new Vector3f(0f, -1f, 0f))

  /** Loads the all the spatial of blocks */
  private def loadCache(assetManager: AssetManager, blocks: Set[Block]): Map[BlockModelId, Spatial] = {
    val cacheTry = for {
      block <- blocks
      status <- statusKeys(block.template)
    } yield (block.id, status) -> Try {
      val spat = assetManager.loadModel(status)
      spat.setLocalRotation(rotation(block.rotAngle))
      spat.setLocalTranslation(new Vector3f(block.x, 0f, block.y))
      spat
    }
    cacheTry.foreach { case (_, s) => s.failed.foreach(ex => logger.error(ex.getMessage, ex)) }
    cacheTry.
      filter(_._2.isSuccess).
      map { case (id, suc) => id -> suc.get }.
      toMap
  }

  /** Returns a factory function that builds Spatial for the BlockStatus */
  def factory(assetManager: AssetManager, blocks: Set[Block]): (BlockStatus) => Spatial = {

    val cache = loadCache(assetManager, blocks)
    val f = (status: BlockStatus) =>
      cache(status.block.id, toStatusKey(status))
    f
  }

  /** Returns the status key of a block */
  private def toStatusKey(status: BlockStatus): String = status match {
    case EntryStatus(_) => RedSemModel
    case ExitStatus(_, false) => GreenSemModel
    case ExitStatus(_, true) => RedSemModel
    case PlatformStatus(_, false) => GreenPlatModel
    case PlatformStatus(_, true) => RedPlatModel
  }
}
