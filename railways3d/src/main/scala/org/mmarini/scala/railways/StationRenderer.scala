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
 * Keeps the set of [[Spatial]] to be attached to the scene,
 * the set of [[Spatial]] to be detached from the scene.
 *
 * The cache keeps the Spatial loaded by id
 */
case class StationRenderer(
    assetManager: AssetManager,
    cache: Map[String, Option[Spatial]] = Map(),
    attached: Set[Spatial] = Set(),
    detached: Set[Spatial] = Set(),
    visible: Set[String] = Set()) extends LazyLogging {

  private def load(ids: BlockElementIds, trans: Transform2d) = Try {
    val spat = assetManager.loadModel(ids.templateId).clone
    spat.setLocalRotation(new Quaternion().fromAngleAxis(trans.orientation, OrientationAxis))
    spat.setLocalTranslation(new Vector3f(-trans.translate.getX, 0f, trans.translate.getY))
    for (selId <- ids.selectionId) { spat.setUserData("id", selId) }
    spat
  }

  /** Changes the view of station */
  def change(blocks: Set[BlockStatus]): StationRenderer = {

    // blockStatus 1 ---> elementsIds [0*] --> blockElementId --> elementId
    //                                                        --> templateId
    //                                                        --> selectionId

    // Creates the set visible id
    val ids = for {
      blockStatus <- blocks
      id <- blockStatus.elementIds
    } yield id.elementId

    // Extracts the rendering block set of the spatial not yet loaded
    val newSpatials = for {
      blockStatus <- blocks
      ids <- blockStatus.elementIds
      if (!cache.contains(ids.elementId))
      spatialTry = load(ids, blockStatus.block.trans)
    } yield {
      for { ex <- spatialTry.failed } logger.error(ex.getMessage, ex)
      (ids.elementId, spatialTry.toOption)
    }
    // Adds the new spatial to the cache
    val newCache = cache ++ newSpatials

    val detached = for {
      id <- visible
      if (!ids.contains(id))
      spatial <- newCache(id)
    } yield spatial

    val attached = for {
      id <- ids
      if (!visible.contains(id))
      spatial <- newCache(id)
    } yield spatial

    StationRenderer(assetManager,
      newCache,
      attached,
      detached,
      ids)
  }
}
