/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math.min
import scala.math.max
import scala.math.sqrt
import scala.math.atan2
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model.tracks.Track

/** Describes the status of a trains and creates new status of train in response of action */
trait Train {
  /** Returns the identifier of this train */
  def id: String

  /** Computes the next status after an elapsed time tick */
  def tick(time: Float, gameStatus: GameStatus): (Option[Train], Seq[TrainMessage])

  /** Returns the current route of train */
  def route: TrainRoute

  /** Returns the current linear location of head of this train relative to route begin */
  def location: Float

  /** Returns the number of vehicles composing this train */
  def size: Int

  /** Returns the speed of train */
  def speed: Float

  /** Returns the exit id */
  def exitId: String

  /** Returns the start status */
  def start: Train = this

  /** Returns the stop status */
  def stop: Train = this

  /** Returns true if train has loaded passengers at platform */
  def loaded: Boolean

  private lazy val head = Head(s"$id head", route, location)

  /** Returns the train vehicle composing this train */
  lazy val vehicles: Set[Vehicle] = {
    val mid = for {
      i <- 1 to size - 2
      coach <- Coach(s"$id coach $i", route, location - i * CoachLength)
    } yield coach
    val tail = Tail(s"$id tail", route, location - (size - 1) * CoachLength)
    (head.toSet) ++ mid ++ (tail.toSet)
  }

  /** Extracts the transit tracks */
  lazy val transitTracks: Seq[Track] = route.pathTracks(location - length, location)

  /** Creates the new train status apply a new route */
  def apply(route: TrainRoute, location: Float): Train

  /** Computes the track location of tail */
  def trackTailLocation: Option[(Track, Float)] =
    route.trackLocationAt(location - length)

  /** Returns train length */
  val length = size * CoachLength

  /** Creates the reverse train */
  def reverse(createReverseRoute: (Track, Float, String) => (TrainRoute, Float)): Option[Train] = None

  /** Returns the head location */
  def locationAt(distance: Float): Option[Vector2f] =
    route.locationAt(location - distance)

  /** Returns the head location */
  def directionAt(headDistance: Float, ds: Float): Option[Float] =
    for {
      p0 <- locationAt(headDistance)
      p1 <- locationAt(headDistance - ds)
    } yield {
      val dp = p0.subtract(p1)
      atan2(dp.getX, -dp.getY).toFloat
    }
}
