/**
 *
 */
package org.mmarini.scala.railways.model.tracks

import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model.Transform2d

/**
 * Describes a right curve part of trajectory
 *
 * @constructor create the curve specifying the center,
 *             the radius, the starting angle and the length of track
 *
 */
case class RightCurveTrack(center: Vector2f, radius: Float, begin: Float, length: Float)
    extends CurveTrack(center, radius, begin, length) {

  /** Computes the angle >= 0 from the begin at a given distance */
  def angle(distance: Float): Float = distance / radius

  /** Returns the backward track of this */
  def backward: Track = new LeftCurveTrack(center, radius, begin + angle(length), length)

  /** Creates a segment track applying the transformation */
  def apply(tran: Transform2d): RightCurveTrack = RightCurveTrack(tran(center), radius, tran(begin), length)
}
