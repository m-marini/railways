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
case class WaitingForTrackTrain(
  id: String,
  size: Int,
  loaded: Boolean,
  route: TrainRoute,
  location: Float,
  exitId: String) extends Train
    with NoMoveTrain
    with LazyLogging {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float) =
    WaitingForTrackTrain(id, size, loaded, route, location, exitId)

  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus) = {
    // Computes the target speed by braking space
    val targetSpeed = min(max(MinSpeed, sqrt(2 * MaxDeceleration * (route.length - location)).toFloat), MaxSpeed)
    // Computes the acceleration
    val acc = min(max(-MaxDeceleration, (targetSpeed) / time), MaxAcceleration)
    // Computes the real speed
    val newSpeed = acc * time
    // Computes the new location
    val newLocation = location + newSpeed * time

    if (newLocation > route.length - MinDistance) {
      (Some(this), Seq())
    } else {
      (Some(
        MovingTrain(id, size, loaded, route, newLocation, newSpeed, exitId)),
        Seq(TrainStartedMsg(id)))
    }
  }

  /** Creates the reverse train */
  override def reverse(createReverseRoute: (Track, Float, String) => (TrainRoute, Float)) = {
    val Some((headTrack, headLocation)) = route.trackLocationAt(location)
    for {
      (trackTail, _) <- trackTailLocation if (!trackTail.isInstanceOf[EntryTrack])
    } yield {
      val (revRoute, revLocation) = createReverseRoute(headTrack, headLocation, id)
      MovingTrain(id, size, loaded, revRoute, revLocation + length, 0f, exitId)
    }
  }

  /** Creates toogle status */
  override def stop =
    StoppedTrain(id, size, loaded, route, location, exitId)
}
