/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model.tracks.Track

/**
 * Contains the sequence of Block the train occupies and will pass along the direction.
 *
 * Computes the location along the route
 *
 */
case class TrainRoute(tracks: IndexedSeq[Track]) {

  /** Sequence of distance of start point of the track and track itself of the route */
  private val mapper = (0f +: tracks.init.foldLeft(Seq.empty[Float])((list, track) => list match {
    case head +: _ => (track.length + head) +: list
    case _ => Seq(track.length)
  }).reverse).
    zip(tracks)

  /** Returns the length of the route */
  val length: Float = tracks.map(_.length).sum

  /** Returns the track and the distance from begin of a point in the route at a given distance of the start route */
  def trackLocationAt(distance: Float): Option[(Track, Float)] =
    for (
      (len, track) <- mapper.find {
        case (len, track) => distance >= len && distance <= len + track.length
      }
    ) yield (track, distance - len)

  /** Returns the location of a point in the route at a given distance of the start route */
  def locationAt(distance: Float): Option[Vector2f] =
    for {
      (track, dist) <- trackLocationAt(distance)
      loc <- track.locationAt(dist)
    } yield loc

  /** Returns the last track */
  def last: Track = tracks.last

  /** Returns the tracks between two points in the route */
  def pathTracks(start: Float, end: Float): Seq[Track] =
    mapper.filter {
      case (x, track) => x + track.length >= start && x <= end
    }.map { case (_, t) => t }

  //  /** Returns the route starting from a specific point dropping the initial tracks and the new distance */
  //  def subRoute(distance: Float): (TrainRoute, Float) = {
  //    val subRoute = TrainRoute(pathTracks(distance, length).toIndexedSeq)
  //    // length - distance = newLenght - newDistance
  //    // => new Distance = distance + newLength - length
  //    (subRoute, distance + subRoute.length - length)
  //  }

}
