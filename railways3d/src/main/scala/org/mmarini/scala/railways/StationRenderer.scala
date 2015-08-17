/**
 *
 */
package org.mmarini.scala.railways

import scala.collection.JavaConversions.asScalaBuffer
import scala.util.Try
import com.jme3.asset.AssetManager
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model.blocks.ExitStatus
import org.mmarini.scala.railways.model.blocks.PlatformStatus
import org.mmarini.scala.railways.model.blocks.ExitBlock
import org.mmarini.scala.railways.model.blocks.SegmentBlock
import org.mmarini.scala.railways.model.StationStatus
import org.mmarini.scala.railways.model.blocks.SwitchStatus
import org.mmarini.scala.railways.model.blocks.RightHandSwitchBlock
import org.mmarini.scala.railways.model.blocks.SegmentStatus
import org.mmarini.scala.railways.model.blocks.PlatformBlock
import org.mmarini.scala.railways.model.blocks.LeftHandSwitchBlock
import org.mmarini.scala.railways.model.blocks.EntryBlock
import org.mmarini.scala.railways.model.blocks.BlockStatus
import org.mmarini.scala.railways.model.blocks.Block
import org.mmarini.scala.railways.model.blocks.EntryStatus
import org.mmarini.scala.railways.model.blocks.BlockElementIds
import org.mmarini.scala.railways.model.Transform2d

/**
 * Handles the events of simulation coming from user or clock ticks
 *
 * Each event generates a change of rendered model 3d.
 * The model is kept in this Game
 *
 * This Game constructor initializes the game status
 */
case class StationRenderer(
    assetManager: AssetManager,
    cache: Map[String, Option[Spatial]] = Map(),
    attached: Set[Spatial] = Set(),
    detached: Set[Spatial] = Set()) extends LazyLogging {

  private def load(ids: BlockElementIds, trans: Transform2d) = Try {
    val spat = assetManager.loadModel(ids.templateId).clone
    spat.setLocalRotation(new Quaternion().fromAngleAxis(trans.orientation, OrientationAxis))
    spat.setLocalTranslation(new Vector3f(-trans.translate.getX, 0f, trans.translate.getY))
    for (selId <- ids.selectionId) { spat.setUserData("id", selId) }
    spat
  }

  /** Changes the view of station */
  def change(blocks: Set[BlockStatus]): StationRenderer = {
    val newSpatials = for {
      blockStatus <- blocks
      ids <- blockStatus.elementIds
      if (!cache.contains(ids.elementId))
    } yield {
      val spatialTry = load(ids, blockStatus.block.trans)
      for { ex <- spatialTry.failed } logger.error(ex.getMessage, ex)
      (ids.elementId, spatialTry.toOption)
    }

    val newCache = cache ++ newSpatials

    val ids = for {
      blockStatus <- blocks
      id <- blockStatus.elementIds
    } yield id.elementId

    val detached = for {
      (id, spatialOpt) <- cache
      if (!ids.contains(id))
      spatial <- spatialOpt
    } yield spatial

    val attached = for {
      id <- ids
      spatial <- newCache(id)
    } yield spatial

    StationRenderer(assetManager,
      newCache,
      attached,
      detached.toSet)
  }
}
