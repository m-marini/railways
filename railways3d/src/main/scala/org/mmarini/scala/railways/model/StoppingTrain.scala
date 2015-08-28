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
case class StoppingTrain(
    id: String,
    size: Int,
    loaded: Boolean,
    route: TrainRoute,
    location: Float,
    speed: Float,
    exitId: String) extends Train with LazyLogging {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float): Train = StoppingTrain(id, size, loaded, route, location, speed, exitId)

  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus): (Option[Train], Seq[TrainMessage]) = {
    // Computes the new speed
    val newSpeed = max(speed - MaxDeceleration * time, 0f)

    if (newSpeed <= 0f) {
      (Some(StoppedTrain(id, size, loaded, route, location, exitId)),
        Seq(TrainStoppedMsg(id)))
    } else {
      // Computes the new location
      val newLocation = location + newSpeed * time
      if (newLocation >= route.length) {
        (Some(
          StoppedTrain(id, size, loaded, route, route.length, exitId)),
          Seq(TrainStoppedMsg(id)))
      } else {
        (Some(
          StoppingTrain(id, size, loaded, route, newLocation, newSpeed, exitId)),
          Seq())
      }
    }
  }

  /** Creates toogle status */
  override def start: Train =
    MovingTrain(id, size, loaded, route, location, speed, exitId)
}
