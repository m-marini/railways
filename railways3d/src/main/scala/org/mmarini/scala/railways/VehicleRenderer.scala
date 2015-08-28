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
case class VehicleRenderer(
    assetManager: AssetManager,
    vehicleSpatialsSet: Set[(Vehicle, Spatial)] = Set(),
    attached: Set[Spatial] = Set(),
    detached: Set[Spatial] = Set()) extends LazyLogging {

  private val HeadModel = "Textures/veichles/head.blend"
  private val TailModel = "Textures/veichles/tail.blend"
  private val CoachModel = "Textures/veichles/coach.blend"

  /** Renders the trains */
  def apply(vehicles: Set[Vehicle]): VehicleRenderer = {
    // Extracts vehicle identifiers
    val vehicleIds = vehicles.map(_.id)

    // Splits the cache into keep/remove spatials
    val (keepCache, removeCache) = vehicleSpatialsSet.partition(entry => vehicleIds.contains(entry._1.id))

    val spatialById = (for {
      (v, s) <- keepCache
    } yield (v.id -> s)).toMap

    // Splits the cached/new vehicles
    val (cachedVehicles, newVehicles) = vehicles.partition(v => spatialById.contains(v.id))

    // Creates the new renderer with new vehicles
    val addedCache =
      for {
        vehicle <- newVehicles
        spatial <- createSpatial(vehicle)
      } yield {
        (vehicle, spatial)
      }

    val newKeepCache = for {
      vehicle <- cachedVehicles
      spatial <- spatialById.get(vehicle.id)
    } yield (vehicle, spatial)

    val attached = for {
      (_, spatial) <- addedCache
    } yield spatial

    val detached = for {
      (_, spatial) <- removeCache
    } yield spatial

    VehicleRenderer(
      assetManager,
      addedCache ++ newKeepCache,
      attached,
      detached)

  }

  /** Creates train spatial */
  private def createSpatial(vehicle: Vehicle): Option[Spatial] = {
    val spatialTry = vehicle match {
      case _: Head => Try(assetManager.loadModel(HeadModel).clone)
      case _: Coach => Try(assetManager.loadModel(CoachModel).clone)
      case _: Tail => Try(assetManager.loadModel(TailModel).clone)
    }
    spatialTry.failed.foreach(ex => logger.error(ex.getMessage, ex))
    spatialTry.toOption
  }
}
