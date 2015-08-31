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
    exitId: String,
    creationTime: Float) extends Train with LazyLogging {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float): Train = StoppingTrain(
    id = id,
    size = size,
    loaded = loaded,
    route = route,
    location = location,
    speed = speed,
    exitId = exitId,
    creationTime = creationTime)

  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus): (Option[Train], Seq[TrainMessage]) = {
    // Computes the new speed
    val newSpeed = max(speed - MaxDeceleration * time, 0f)

    if (newSpeed <= 0f) {
      (Some(StoppedTrain(
        id = id,
        size = size,
        loaded = loaded,
        route = route,
        location = location,
        exitId = exitId,
        creationTime = creationTime)),
        Seq(TrainStoppedMsg(id)))
    } else {
      // Computes the new location
      val newLocation = location + newSpeed * time
      if (newLocation >= route.length) {
        (Some(StoppedTrain(
          id = id,
          size = size,
          loaded = loaded,
          route = route,
          location = route.length,
          exitId = exitId,
          creationTime = creationTime)),
          Seq(TrainStoppedMsg(id)))
      } else {
        (Some(StoppingTrain(
          id = id,
          size = size,
          loaded = loaded,
          route = route,
          location = newLocation,
          speed = newSpeed,
          exitId = exitId,
          creationTime = creationTime)),
          Seq())
      }
    }
  }

  /** Creates toogle status */
  override def start: Train =
    MovingTrain(id = id,
      size = size,
      loaded = loaded,
      route = route,
      location = location,
      speed = speed,
      exitId = exitId,
      creationTime = creationTime)
}
