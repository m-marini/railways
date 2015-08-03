/**
 *
 */
package org.mmarini.scala.railways.model.tracks

import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model.Transform2d

/**
 * Describes a linear part of trajectory
 *
 * @constructor creates the segment specifying the starting point and the ending point
 *
 */
case class PlatformTrack(begin: Vector2f, end: Vector2f) extends LinearTrack {
  /** Return the backward track of this track */
  override def backward = Some(PlatformTrack(end, begin))

  /** Creates a segment track applying the transformation */
  def apply(tran: Transform2d): PlatformTrack = PlatformTrack(tran(begin), tran(end))
}
