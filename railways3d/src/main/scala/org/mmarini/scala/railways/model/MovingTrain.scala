/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math.min
import scala.math.max
import scala.math.sqrt
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model.tracks.PlatformTrack
import org.mmarini.scala.railways.model.blocks.ExitStatus
/** This MovingTrain computes the next state of this moving train */
case class MovingTrain(
    id: String,
    size: Int,
    route: TrainRoute,
    location: Float,
    speed: Float) extends Train with LazyLogging {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float): Train = MovingTrain(id, size, route, location, speed)

  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus): Option[Train] = {
    // Computes the target speed by braking space
    val targetSpeed = min(max(MinSpeed, sqrt(2 * MaxDeceleration * (route.length - location)).toFloat), MaxSpeed)
    // Computes the acceleration
    val acc = min(max(-MaxDeceleration, (targetSpeed - speed) / time), MaxAcceleration)
    // Computes the real speed
    val newSpeed = speed + acc * time
    // Computes the new location
    val newLocation = location + newSpeed * time

    if (newLocation >= route.length) {
      route.last match {
        case _: ExitStatus => None
        case _: PlatformTrack =>
          logger.debug(s"$id waiting for passenger")
          Some(WaitForPassengerTrain(id, size, route, route.length, BoardingTime))
        case _ =>
          logger.debug(s"$id stopped")
          Some(WaitForRouteTrain(id, size, route, route.length))
      }
    } else {
      Some(MovingTrain(id, size, route, newLocation, newSpeed))
    }
  }
}
