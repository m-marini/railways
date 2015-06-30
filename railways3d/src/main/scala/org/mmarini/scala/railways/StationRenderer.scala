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
import org.mmarini.scala.railways.model.GameStatus
import org.mmarini.scala.railways.model.LeftHandSwitch
import org.mmarini.scala.railways.model.Platform
import org.mmarini.scala.railways.model.PlatformStatus
import org.mmarini.scala.railways.model.RightHandSwitch
import org.mmarini.scala.railways.model.SwitchStatus
import com.jme3.asset.AssetManager
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.typesafe.scalalogging.LazyLogging
import scala.collection.JavaConversions._
import org.mmarini.scala.railways.model.StationStatus
import org.mmarini.scala.railways.model.Segment
import org.mmarini.scala.railways.model.SegmentStatus

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

  private val SemRedModel = "Textures/blocks/sem-red.blend"
  private val SemGreenModel = "Textures/blocks/sem-green.blend"

  private val GreenPlatModel = "Textures/blocks/plat-green.blend"
  private val RedPlatModel = "Textures/blocks/plat-red.blend"

  private val SwitchLeftStraightGreenModel = "Textures/blocks/swi-left-str-green.blend"
  private val SwitchLeftStraightRedModel = "Textures/blocks/swi-left-str-red.blend"
  private val SwitchLeftDivGreenModel = "Textures/blocks/swi-left-div-green.blend"
  private val SwitchLeftDivRedModel = "Textures/blocks/swi-left-div-red.blend"

  private val SwitchRightStraightGreenModel = "Textures/blocks/swi-right-str-green.blend"
  private val SwitchRightStraightRedModel = "Textures/blocks/swi-right-str-red.blend"
  private val SwitchRightDivGreenModel = "Textures/blocks/swi-right-div-green.blend"
  private val SwitchRightDivRedModel = "Textures/blocks/swi-right-div-red.blend"

  // Creates all the spatial names of a block template
  private val StatusKeys: Map[BlockTemplate, Set[String]] = Map(
    Entry -> Set(SemRedModel),
    Exit -> Set(
      SemRedModel,
      SemGreenModel),
    Segment -> Set(
      RedPlatModel,
      GreenPlatModel),
    Platform -> Set(
      RedPlatModel,
      GreenPlatModel),
    LeftHandSwitch -> Set(
      SwitchLeftStraightRedModel,
      SwitchLeftStraightGreenModel,
      SwitchLeftDivRedModel,
      SwitchLeftDivGreenModel),
    RightHandSwitch -> Set(
      SwitchRightStraightGreenModel,
      SwitchRightDivGreenModel,
      SwitchRightStraightRedModel,
      SwitchRightDivRedModel))

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
          spat.setLocalRotation(new Quaternion().fromAngleAxis(block.orientation, OrientationAxis))
          spat.setLocalTranslation(new Vector3f(-block.x, 0f, block.y))
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

  /** Finds geometry block name by geometry */
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
  def change(status: StationStatus) {
    // Render each block
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
    // Render each train
  }

  /** Returns the status key of a block */
  def statusKey(status: BlockStatus): String = status match {
    case EntryStatus(_) => SemRedModel
    case s: ExitStatus if (s.free) => SemGreenModel
    case s: ExitStatus => SemRedModel

    case s: PlatformStatus if (s.free) => GreenPlatModel
    case s: PlatformStatus => RedPlatModel

    case s: SegmentStatus if (s.free) => GreenPlatModel
    case s: SegmentStatus => RedPlatModel

    case s: SwitchStatus if (s.block.template == LeftHandSwitch && s.diverging && s.free) => SwitchLeftDivGreenModel
    case s: SwitchStatus if (s.block.template == LeftHandSwitch && s.diverging && !s.free) => SwitchLeftDivRedModel
    case s: SwitchStatus if (s.block.template == LeftHandSwitch && !s.diverging && s.free) => SwitchLeftStraightGreenModel
    case s: SwitchStatus if (s.block.template == LeftHandSwitch && !s.diverging && !s.free) => SwitchLeftStraightRedModel
    case s: SwitchStatus if (s.diverging && s.free) => SwitchRightDivGreenModel
    case s: SwitchStatus if (s.diverging && !s.free) => SwitchRightDivRedModel
    case s: SwitchStatus if (s.free) => SwitchRightStraightGreenModel
    case s: SwitchStatus => SwitchRightStraightRedModel
  }
}
