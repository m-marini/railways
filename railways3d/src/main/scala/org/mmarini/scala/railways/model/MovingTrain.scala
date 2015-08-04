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
import org.mmarini.scala.railways.model.blocks.EntryStatus
import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.tracks.ExitTrack
import org.mmarini.scala.railways.model.tracks.EntryTrack
import org.mmarini.scala.railways.model.tracks.EntryTrack
import org.mmarini.scala.railways.model.tracks.ExitTrack
import com.sun.media.sound.Platform
import org.mmarini.scala.railways.model.tracks.PlatformTrack

/** This MovingTrain computes the next state of this moving train */
case class MovingTrain(
    id: String,
    size: Int,
    loaded: Boolean,
    route: TrainRoute,
    location: Float,
    speed: Float) extends Train with LazyLogging {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float): Train = MovingTrain(id, size, loaded, route, location, speed)

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
      this.trackTailLocation match {
        case Some((track, _)) if (track.isInstanceOf[ExitTrack]) =>
          logger.debug(s"$id exited")
          None
        case Some((track, _)) if (track.isInstanceOf[PlatformTrack] && !loaded) =>
          logger.debug(s"$id waiting for passenger")
          Some(WaitForPassengerTrain(id, size, route, route.length, BoardingTime))
        case _ =>
          Some(MovingTrain(id, size, loaded, route, route.length, 0))
      }
    } else {
      Some(MovingTrain(id, size, loaded, route, newLocation, newSpeed))
    }
  }

  /** Creates the reverse train */
  override def reverse(createReverseRoute: (Track, Float, String) => (TrainRoute, Float)) =
    if (speed == 0) {
      val Some((headTrack, headLocation)) = route.trackLocationAt(location)
      for {
        (trackTail, _) <- trackTailLocation if (!trackTail.isInstanceOf[EntryTrack])
      } yield {
        val (revRoute, revLocation) = createReverseRoute(headTrack, headLocation, id)
        logger.debug("Train {} reversed", id)
        MovingTrain(id, size, loaded, revRoute, revLocation + length, 0f)
      }
    } else {
      None
    }

  /** Creates toogle status */
  override def toogleStatus = {
    logger.debug("Stopping train {}", id)
    StoppingTrain(id, size, loaded, route, location, speed)
  }
}

/** A factory of [[MovingTrain]] */
object MovingTrain {

  /** Creates a [[MovingTrain]] */
  def apply(id: String, size: Int, entry: EntryStatus): MovingTrain = {
    val route = TrainRoute(IndexedSeq(entry.block.entryTrack.asInstanceOf[Track]))
    MovingTrain(id = id,
      location = route.length,
      loaded = false,
      size = size,
      route = route,
      speed = MaxSpeed)
  }
}