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
class StationRenderer(
    blocks: Set[Block],
    assetManager: AssetManager,
    rootNode: Node) extends LazyLogging {

  // Creates cache
  private var cache = Map[String, Spatial]()

  private def getCached(ids: BlockElementIds, trans: Transform2d): Option[Spatial] = {

    def load(ids: BlockElementIds): Try[Spatial] = {
      val spatialTry = Try {
        val spat = assetManager.loadModel(ids.templateId).clone
        spat.setLocalRotation(new Quaternion().fromAngleAxis(trans.orientation, OrientationAxis))
        spat.setLocalTranslation(new Vector3f(-trans.translate.getX, 0f, trans.translate.getY))
        for (selId <- ids.selectionId) { spat.setUserData("id", selId) }
        spat
      }

      // Dump loading errors
      spatialTry.failed.foreach(ex => logger.error(ex.getMessage, ex))
      spatialTry
    }

    val spatial = cache.get(ids.elementId)
    if (spatial.isEmpty) {
      val spTry = load(ids)
      if (spTry.isSuccess) {
        cache = cache + (ids.elementId -> spTry.get)
      }
      spTry.toOption
    } else {
      spatial
    }
  }

  /** Changes the view of station */
  def change(status: StationStatus) {
    // Render each block
    val ids = (for {
      blockStatus <- status.blocks.values
      id <- blockStatus.elementIds
      spatial <- getCached(id, blockStatus.block.trans)
    } yield {
      rootNode.attachChild(spatial)
      id.elementId
    }).toSet
    for {
      (id, spatial) <- cache
    } {
      if (!ids.contains(id))
        rootNode.detachChild(spatial)
    }
  }
}
