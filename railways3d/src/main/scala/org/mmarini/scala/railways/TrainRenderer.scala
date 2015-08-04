/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.Train
import com.typesafe.scalalogging.LazyLogging
import com.jme3.scene.Spatial
import com.jme3.asset.AssetManager
import com.jme3.scene.Node
import com.jme3.math.Vector3f
import com.jme3.math.Quaternion
import org.mmarini.scala.railways.model.Vehicle
import scala.util.Try
import org.mmarini.scala.railways.model.Head
import org.mmarini.scala.railways.model.Coach
import org.mmarini.scala.railways.model.Tail

/**
 * @author us00852
 *
 */
class TrainRenderer(vehicleCache: Map[String, Spatial], target: Node, assetManager: AssetManager) extends LazyLogging {
  private val HeadModel = "Textures/veichles/head.blend"
  private val TailModel = "Textures/veichles/tail.blend"
  private val CoachModel = "Textures/veichles/coach.blend"

  /** Renders the trains */
  def render(vehicles: Set[Vehicle]): TrainRenderer = {
    // Extracts vehicle identifiers
    val vehicleIds = vehicles.map(_.id)

    // Splits the cache into keep/remove spatials 
    val (keep, remove) = vehicleCache.partition(entry => vehicleIds.contains(entry._1))

    // Removes the obsolete spatials
    for ((_, spatial) <- remove)
      target.detachChild(spatial)

    // Splits the cached/new vehicles
    val (cachedVehicles, newVehicles) = vehicles.partition(v => keep.contains(v.id))

    // Creates the new renderer with new vehicles
    val newCache = keep ++ (
      for {
        vehicle <- newVehicles
        spatial <- createSpatial(vehicle)
      } yield {
        spatial.setUserData("id", s"train ${vehicle.id}")
        (vehicle.id -> spatial)
      })

    // Move all cached spatials
    for {
      vehicle <- cachedVehicles
      spatial <- keep.get(vehicle.id)
    } {
      val pos = new Vector3f(
        -vehicle.location.x,
        0,
        vehicle.location.y)
      spatial.setUserData("id", s"train ${vehicle.id}")
      spatial.setLocalTranslation(pos)
      spatial.setLocalRotation(new Quaternion().fromAngleNormalAxis(vehicle.orientation, OrientationAxis))
    }

    setTrains(keep ++ newCache)
  }

  /** Creates a new renderer with a new cache */
  def setTrains(trainCache: Map[String, Spatial]): TrainRenderer =
    new TrainRenderer(trainCache, target, assetManager)

  /** Creates train spatial */
  private def createSpatial(vehicle: Vehicle): Option[Spatial] = {
    val spatialTry = vehicle match {
      case _: Head => Try(assetManager.loadModel(HeadModel)).map(_.clone)
      case _: Coach => Try(assetManager.loadModel(CoachModel)).map(_.clone)
      case _: Tail => Try(assetManager.loadModel(TailModel)).map(_.clone)
    }
    spatialTry.failed.foreach(ex => logger.error(ex.getMessage, ex))
    val spatialOpt = spatialTry.toOption
    spatialOpt.foreach(target.attachChild)
    spatialOpt
  }
}

object TrainRenderer {
  /** Creates an empty renderer */
  def apply(target: Node, assetManager: AssetManager): TrainRenderer = {
    new TrainRenderer(Map.empty, target, assetManager);
  }
}
