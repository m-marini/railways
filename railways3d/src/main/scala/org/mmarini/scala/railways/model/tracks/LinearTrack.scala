/**
 *
 */
package org.mmarini.scala.railways.model.tracks

import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model.tracks.Track

/**
 * Describes a linear part of trajectory
 *
 * @constructor creates the segment specifying the starting point and the ending point
 *
 */
trait LinearTrack extends Track {

  def begin: Vector2f

  def end: Vector2f

  /** Returns the length of the track */
  override val length = end.subtract(begin).length

  /** Returns the point at a given distance from begin point*/
  override def locationAt(distance: Float): Option[Vector2f] =
    if (distance >= 0 && distance <= length) {
      Some(begin.add(end.subtract(begin).mult(distance / length)))
    } else {
      None
    }
}
