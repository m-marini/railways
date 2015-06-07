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
  private val RedPlatModel = "Textures/blocks/plat-red.blend"
  private val GreenPlatModel = "Textures/blocks/plat-green.blend"
  private val GreenLeftDevModel = "Textures/blocks/dev-left-dir-green.blend"
  private val RedLeftDevModel = "Textures/blocks/dev-left-dir-red.blend"
  private val GreenRightDevModel = "Textures/blocks/dev-right-dir-green.blend"
  private val RedRightDevModel = "Textures/blocks/dev-right-dir-red.blend"

  // Creates all the spatial names of a block template
  private val StatusKeys: Map[BlockTemplate, Set[String]] = Map(
    Entry -> Set(RedSemModel),
    Exit -> Set(
      RedSemModel,
      GreenSemModel),
    TrackTemplate -> Set(
      RedPlatModel,
      GreenPlatModel),
    Platform -> Set(
      RedPlatModel,
      GreenPlatModel),
    LeftDeviator -> Set(
      GreenLeftDevModel,
      RedLeftDevModel),
    RightDeviator -> Set(
      GreenRightDevModel,
      RedRightDevModel))

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
    case PlatformStatus(_, busy) => if (busy) RedPlatModel else GreenPlatModel
    case DeviatorStatus(b, false, _) => if (b.template == LeftDeviator) GreenLeftDevModel else GreenRightDevModel
    case DeviatorStatus(b, true, _) => if (b.template == LeftDeviator) RedLeftDevModel else RedRightDevModel
  }
}
