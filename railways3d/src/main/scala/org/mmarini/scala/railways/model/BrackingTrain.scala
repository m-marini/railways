/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math.min
import scala.math.max
import scala.math.sqrt
import com.typesafe.scalalogging.LazyLogging

/** This BrakingTrain computes the next state of this braking train */
case class BrakingTrain(
    id: String,
    size: Int,
    route: TrainRoute,
    location: Float,
    speed: Float) extends Train with LazyLogging {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float): Train = BrakingTrain(id, size, route, location, speed)

  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus): Option[Train] = {
    // Computes the new speed
    val newSpeed = max(speed - MaxDeceleration * time, 0f)

    if (newSpeed <= 0f) {
      Some(StoppedTrain(id, size, route, location))
    } else {
      // Computes the new location
      val newLocation = location + newSpeed * time
      if (newLocation >= route.length) {
        Some(StoppedTrain(id, size, route, route.length))
      } else {
        Some(BrakingTrain(id, size, route, newLocation, newSpeed))
      }
    }
  }

  /** Creates toogle status */
  override def toogleStatus = {
    logger.debug("Go train {}", id)
    MovingTrain(id, size, route, location, speed)
  }
}
