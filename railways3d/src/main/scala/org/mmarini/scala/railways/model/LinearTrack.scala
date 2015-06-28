/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import com.jme3.math.Vector3f

/**
 * Describes a linear part of trajectory
 *
 * @constructor create the segment specifying the starting point and the ending point
 *
 */
case class LinearTrack(begin: Vector2f, end: Vector2f) extends Track {
  /** */
  override val length = end.subtract(begin).length

  /** */
  override def locationAt(distance: Float): Option[Vector2f] =
    if (distance >= 0 && distance <= length) {
      Some(begin.add(end.subtract(begin).mult(distance / length)))
    } else {
      None
    }

}
