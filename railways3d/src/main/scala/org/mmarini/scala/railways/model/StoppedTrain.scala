/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math.min
import scala.math.max
import scala.math.sqrt
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.tracks.HiddenTrack

/** This StoppedTrain computes the next state of this stopped train */
case class StoppedTrain(
    id: String,
    size: Int,
    route: TrainRoute,
    location: Float) extends Train with LazyLogging {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float): Train = StoppedTrain(id, size, route, location)

  /** Computes the next status after an elapsed time tick */
  def tick(time: Float, gameStatus: GameStatus): Option[Train] = Some(this)

  /** Creates toogle status */
  override def toogleStatus = {
    logger.debug("Go train {}", id)
    MovingTrain(id, size, route, location, 0f)
  }

  /** Creates the reverse train */
  override def reverse(createReverseRoute: (Track, Float, String) => (TrainRoute, Float)) = {
    val Some((headTrack, headLocation)) = route.trackLocationAt(location)
    for {
      (trackTail, _) <- trackTailLocation if (!trackTail.isInstanceOf[HiddenTrack])
    } yield {
      val (revRoute, revLocation) = createReverseRoute(headTrack, headLocation, id)
      logger.debug("Train {} reversed", id)
      MovingTrain(id, size, revRoute, revLocation + length, 0f)
    }
  }
}
