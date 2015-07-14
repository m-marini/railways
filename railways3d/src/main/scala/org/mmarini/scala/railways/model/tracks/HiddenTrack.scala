/**
 *
 */
package org.mmarini.scala.railways.model.tracks

import com.jme3.math.Vector2f

/**
 * Describes a linear part of trajectory
 *
 * @constructor creates the segment specifying the starting point and the ending point
 *
 */
case object HiddenTrack extends Track {
  /** */
  override val length = Float.MaxValue

  /** */
  override def locationAt(distance: Float): Option[Vector2f] = None
}
