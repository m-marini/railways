/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math.min
import scala.math.max
import scala.math.sqrt
import com.typesafe.scalalogging.LazyLogging

/** Describes the status of a trains and creates new status of train in response of action */
trait Train {
  /** Returns the identifier of this train */
  def id: String

  /** Computes the next status after an elapsed time tick */
  def tick(time: Float, gameStatus: GameStatus): GameStatus

  /** Returns the current route of train */
  def route: TrainRoute

  /** Returns the current linear location of head of this train relative to route begin */
  def location: Float

  /** Returns the number of vehicles composing this train */
  def size: Int

  /** Returns the train vehicle composing this train */
  val vehicles: Set[Vehicle] = {
    val head = Head(s"$id.head", route, location)
    val mid = for {
      i <- 1 to size - 2
      coach <- Coach(s"$id.coach.$i", route, location - i * CoachLength)
    } yield coach
    val tail = Tail(s"$id.tail", route, location - (size - 1) * CoachLength)
    (head.toSet) ++ mid ++ (tail.toSet)
  }

  lazy val transitTracks: Seq[Track] = route.pathTracks(location - size * CoachLength, location)
}

/** This MovingTrain computes the next state of this moving train */
case class MovingTrain(id: String, size: Int, route: TrainRoute, location: Float, speed: Float) extends Train with LazyLogging {

  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus): GameStatus = {
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
        case _: ExitStatus => gameStatus.removeTrain(this)
        case _: PlatformTrack =>
          logger.debug(s"$id waiting for passenger")
          gameStatus.putTrain(WaitForPassengerTrain(id, size, route, route.length, BoardingTime))
        case _ =>
          logger.debug(s"$id stopped")
          gameStatus.putTrain(WaitForRouteTrain(id, size, route, route.length))
      }
    } else {
      gameStatus.putTrain(MovingTrain(id, size, route, newLocation, newSpeed))
    }
  }
}

/** This BrakingTrain computes the next state of this braking train */
case class BrakingTrain(id: String, size: Int, route: TrainRoute, location: Float, speed: Float) extends Train with LazyLogging {

  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus): GameStatus = {
    // Computes the new speed
    val newSpeed = max(speed - MaxDeceleration * time, 0f)

    if (newSpeed <= 0f) {
      gameStatus.putTrain(StoppedTrain(id, size, route, location))
    } else {
      // Computes the new location
      val newLocation = location + newSpeed * time
      if (newLocation >= route.length) {
        gameStatus.putTrain(StoppedTrain(id, size, route, route.length))
      } else {
        gameStatus.putTrain(MovingTrain(id, size, route, newLocation, newSpeed))
      }
    }
  }
}

/** This WaitForRouteTrain computes the next state of this train that is waiting for route availability */
case class WaitForRouteTrain(id: String, size: Int, route: TrainRoute, location: Float) extends Train with LazyLogging {
  /** Computes the next status after an elapsed time tick */
  def tick(time: Float, gameStatus: GameStatus): GameStatus = gameStatus
}

/** This WaitForPassengerTrain computes the next state of this train that is waiting for passenger boarding */
case class WaitForPassengerTrain(id: String, size: Int, route: TrainRoute, location: Float, timeout: Float) extends Train with LazyLogging {
  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus): GameStatus =
    if (timeout - time > 0) {
      gameStatus.putTrain(WaitForPassengerTrain(id, size, route, location, timeout - time))
    } else {
      logger.debug(s"$id stopped")
      gameStatus.putTrain(StoppedTrain(id, size, route, location))
    }
}

/** This StoppedTrain computes the next state of this stopped train */
case class StoppedTrain(id: String, size: Int, route: TrainRoute, location: Float) extends Train {
  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus): GameStatus = gameStatus
}
