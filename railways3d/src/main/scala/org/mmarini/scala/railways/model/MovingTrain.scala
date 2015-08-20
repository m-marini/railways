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
    speed: Float,
    exitId: String) extends Train with LazyLogging {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float): Train =
    MovingTrain(id, size, loaded, route, location, speed, exitId)

  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus) = {
    // Computes the target speed by braking space
    val stopLocation = route.length - MinDistance
    val dist = max(stopLocation - location, 0)

    val targetSpeed = min(max(MinSpeed, sqrt(2 * MaxDeceleration * dist).toFloat), MaxSpeed)
    // Computes the acceleration
    val acc = min(max(-MaxDeceleration, (targetSpeed - speed) / time), MaxAcceleration)
    // Computes the real speed
    val newSpeed = speed + acc * time
    // Computes the new location
    val newLocation = location + newSpeed * time

    if (newLocation >= stopLocation) {
      this.trackTailLocation match {
        case Some((track, _)) if (track.isInstanceOf[ExitTrack]) =>
          (None, Seq(TrainExitedMsg(id)))
        case Some((track, _)) if (track.isInstanceOf[PlatformTrack] && !loaded) =>
          (Some(WaitForPassengerTrain(id, size, route, stopLocation, BoardingTime, exitId)),
            Seq(TrainWaitForReloadMsg(id)))
        case _ =>
          (Some(WaitingForTrackTrain(id, size, loaded, route, stopLocation, exitId)),
            Seq(TrainWaitForTrackMsg(id)))
      }
    } else {
      (Some(
        MovingTrain(id, size, loaded, route, newLocation, newSpeed, exitId)),
        Seq())
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
        MovingTrain(id, size, loaded, revRoute, revLocation + length, 0f, exitId)
      }
    } else {
      None
    }

  /** Creates toogle status */
  override def stop =
    StoppingTrain(id, size, loaded, route, location, speed, exitId)
}

/** A factory of [[MovingTrain]] */
object MovingTrain {

  /** Creates a [[MovingTrain]] */
  def apply(id: String, size: Int, entry: EntryStatus, exitId: String): MovingTrain = {
    val route = TrainRoute(IndexedSeq(entry.block.entryTrack.asInstanceOf[Track]))
    MovingTrain(id = id,
      location = route.length,
      loaded = false,
      size = size,
      route = route,
      speed = MaxSpeed,
      exitId = exitId)
  }
}