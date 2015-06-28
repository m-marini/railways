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

/**
 * @author us00852
 *
 */
class TrainRenderer(trainCache: Map[String, Spatial], target: Node, assetManager: AssetManager) extends LazyLogging {
  private val TrainModel = "Textures/veichles/head.blend"

  /** Renders the trains */
  def render(trains: Set[Train]): TrainRenderer = {
    val nextCache =
      (for {
        train <- trains
        veichle <- train.vehicles
      } yield {
        val spatial = trainCache.get(train.id)
        spatial.foreach(spatial => {
          val pos = new Vector3f(
            -veichle.location.x,
            0,
            veichle.location.y)
          spatial.setLocalTranslation(pos)
          spatial.setLocalRotation(new Quaternion().fromAngleNormalAxis(veichle.orientation, OrientationAxis))
        })
        (train.id, spatial.orElse({
          createSpatial(train)
        }))
      }).filterNot(_._2.isEmpty).
        map {
          case (k, v) => (k, v.get)
        }.toMap
    setTrains(nextCache)
  }

  /** Creates a new renderer with a new cache */
  def setTrains(trainCache: Map[String, Spatial]): TrainRenderer =
    new TrainRenderer(trainCache, target, assetManager)

  /** Creates train spatial */
  private def createSpatial(train: Train): Option[Spatial] = {
    val result = for {
      spatial <- Option(assetManager.loadModel(TrainModel)).map(_.clone)
      veichle <- train.vehicles
    } yield {
      logger.debug(s"create ${spatial}")
      spatial.setLocalTranslation(new Vector3f(
        veichle.location.x,
        0,
        veichle.location.y))
      spatial.setLocalRotation(new Quaternion().fromAngleNormalAxis(veichle.orientation, Vector3f.UNIT_Y))
      target.attachChild(spatial)
      spatial
    }
    result
  }
}

object TrainRenderer {
  /** Creates an empty renderer */
  def apply(target: Node, assetManager: AssetManager): TrainRenderer = {
    new TrainRenderer(Map.empty, target, assetManager);
  }
}
