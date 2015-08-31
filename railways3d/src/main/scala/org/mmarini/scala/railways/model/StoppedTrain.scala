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
import org.mmarini.scala.railways.model.tracks.EntryTrack

/** This StoppedTrain computes the next state of this stopped train */
case class StoppedTrain(
  id: String,
  size: Int,
  loaded: Boolean,
  route: TrainRoute,
  location: Float,
  exitId: String,
  creationTime: Float) extends Train
    with NoMoveTrain
    with LazyLogging {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float): Train = StoppedTrain(
    id = id,
    size = size,
    loaded = loaded,
    route = route,
    location = location,
    exitId = exitId,
    creationTime = creationTime)

  /** Computes the next status after an elapsed time tick */
  def tick(time: Float, gameStatus: GameStatus): (Option[Train], Seq[TrainMessage]) = (Some(this), Seq())

  /** Creates toogle status */
  override def start: Train =
    MovingTrain(id = id,
      size = size,
      loaded = loaded,
      route = route,
      location = location,
      speed = 0f,
      exitId = exitId,
      creationTime = creationTime)

  /** Creates the reverse train */
  override def reverse(createReverseRoute: (Track, Float, String) => (TrainRoute, Float)): Option[Train] = {
    val Some((headTrack, headLocation)) = route.trackLocationAt(location)
    for {
      (trackTail, _) <- trackTailLocation if (!trackTail.isInstanceOf[EntryTrack])
    } yield {
      val (revRoute, revLocation) = createReverseRoute(headTrack, headLocation, id)
      logger.debug("Train {} reversed", id)
      MovingTrain(id = id,
        size = size,
        loaded = loaded,
        route = revRoute,
        location = revLocation + length,
        speed = 0f,
        exitId = exitId,
        creationTime = creationTime)
    }
  }
}
