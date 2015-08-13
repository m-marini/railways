/**
 *
 */
package org.mmarini.scala.railways.model.tracks

import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model._

/**
 * Describes a linear part of trajectory
 *
 * @constructor creates the segment specifying the starting point and the ending point
 *
 */
class ExitTrack extends Track {
  /** */
  override val length = 1000f

  /** */
  override def locationAt(distance: Float): Option[Vector2f] = None

  override def backward = None
}
