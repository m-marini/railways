/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f

/**
 * Contains the sequence of Block the train occupies and will pass along the direction.
 *
 * Computes the location along the route
 *
 */
case class TrainRoute(tracks: IndexedSeq[Track]) {

  private val mapper = (0f +: tracks.init.foldLeft(Seq.empty[Float])((list, track) => list match {
    case head +: _ => (track.length + head) +: list
    case _ => Seq(track.length)
  }).reverse).
    zip(tracks)

  val length: Float = tracks.map(_.length).sum

  /** */
  private def trackLocationAt(distance: Float): Option[(Track, Float)] =
    mapper.find {
      case (len, track) => distance >= len && distance <= len + track.length
    }.map {
      case (len, track) => (track, distance - len)
    }

  /**  */
  def locationAt(distance: Float): Option[Vector2f] =
    for {
      (track, dist) <- trackLocationAt(distance)
      loc <- track.locationAt(dist)
    } yield loc
}
