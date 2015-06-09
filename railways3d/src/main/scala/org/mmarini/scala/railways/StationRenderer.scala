/**
 *
 */
package org.mmarini.scala.railways

import scala.util.Try
import org.mmarini.scala.railways.model.Block
import org.mmarini.scala.railways.model.BlockStatus
import org.mmarini.scala.railways.model.BlockTemplate
import org.mmarini.scala.railways.model.DeviatorStatus
import org.mmarini.scala.railways.model.Entry
import org.mmarini.scala.railways.model.EntryStatus
import org.mmarini.scala.railways.model.Exit
import org.mmarini.scala.railways.model.ExitStatus
import org.mmarini.scala.railways.model.GameStatus
import org.mmarini.scala.railways.model.LeftDeviator
import org.mmarini.scala.railways.model.Platform
import org.mmarini.scala.railways.model.PlatformStatus
import org.mmarini.scala.railways.model.RightDeviator
import com.jme3.asset.AssetManager
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model.TrackTemplate
import com.jme3.scene.Geometry

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

  private val RedSemModel = "Textures/blocks/sem-red.blend"
  private val GreenSemModel = "Textures/blocks/sem-green.blend"

  private val GreenPlatModel = "Textures/blocks/plat-green.blend"

  private val GreenDirLeftDevModel = "Textures/blocks/dev-left-dir-green.blend"
  private val GreenDevLeftDevModel = "Textures/blocks/dev-left-dev-green.blend"

  private val GreenDirRightDevModel = "Textures/blocks/dev-right-dir-green.blend"
  private val GreenDevRightDevModel = "Textures/blocks/dev-right-dev-green.blend"

  // Creates all the spatial names of a block template
  private val StatusKeys: Map[BlockTemplate, Set[String]] = Map(
    Entry -> Set(RedSemModel),
    Exit -> Set(
      RedSemModel,
      GreenSemModel),
    TrackTemplate -> Set(
      GreenPlatModel),
    Platform -> Set(
      GreenPlatModel),
    LeftDeviator -> Set(
      GreenDirLeftDevModel,
      GreenDevLeftDevModel),
    RightDeviator -> Set(
      GreenDirRightDevModel,
      GreenDevRightDevModel))

  // Creates cache
  val cache = loadBlockModel.withDefaultValue(Set())

  /** Load 3d model of blocks */
  private def loadBlockModel: Map[String, Map[String, Spatial]] = {
    val assets = for {
      // For each block
      block <- blocks
    } yield {
      val spatialTrys =
        for { statKey <- StatusKeys(block.template) } // for each status of block load spatials
          yield Try {
          val spat = assetManager.loadModel(statKey).clone
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
  import scala.collection.JavaConversions._

  def findByGeo(g: Geometry): Option[String] = {
    def exists(pred: Spatial => Boolean)(s: Spatial): Boolean =
      if (pred(s)) {
        true
      } else {
        s match {
          case n: Node =>
            n.getChildren.toList.exists(exists(pred))
          case _ =>
            false
        }
      }
    val f = for {
      (n1, m) <- cache
      (n2, spat) <- m
      if (exists(_ == g)(spat))
    } yield {
      n1
    }
    if (f.isEmpty) None else Some(f.head)
  }

  /** Changes the view of station */
  def change(status: GameStatus) {
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
    }
  }

  /** Returns the status key of a block */
  def statusKey(status: BlockStatus): String = status match {
    case EntryStatus(_) => RedSemModel
    case ExitStatus(_, busy) => if (busy) RedSemModel else GreenSemModel
    case PlatformStatus(_, _) => GreenPlatModel
    case DeviatorStatus(b, _, false) => if (b.template == LeftDeviator) GreenDirLeftDevModel else GreenDirRightDevModel
    case DeviatorStatus(b, _, true) => if (b.template == LeftDeviator) GreenDevLeftDevModel else GreenDevRightDevModel
  }
}
