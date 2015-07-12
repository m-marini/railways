/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math.sin
import scala.math.cos

/**
 * Describes a left curve part of trajectory
 *
 * @constructor create the curve specifying the center,
 *             the radius, the starting angle and the length of track
 *
 */
case class LeftCurveTrack(center: Vector2f, radius: Float, begin: Float, length: Float)
  extends CurveTrack(center, radius, begin, length) {

  /** Computes the angle <= 0 from the begin at a given distance */
  def angle(distance: Float): Float = -distance / radius

  /** Returns the reverse track of this */
  override lazy val reverse = RightCurveTrack(center, radius, begin + angle(length), length)
}
