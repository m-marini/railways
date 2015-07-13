/**
 *
 */
package org.mmarini.scala.railways.model.tracks

import com.jme3.math.Vector2f
import scala.math.sin
import scala.math.cos

/**
 * Describes a curve part of trajectory
 *
 * @constructor create the curve specifying the center,
 *             the radius, the starting angle and the length of track
 *
 */

abstract class CurveTrack(center: Vector2f, radius: Float, begin: Float, length: Float) extends Track {
  assert(radius >= 0f)
  assert(length >= 0f)

  /** Returns the point at a given distance from the begin */
  def locationAt(distance: Float): Option[Vector2f] =
    if (distance >= 0 && distance <= length) {
      val alpha = begin + angle(distance)
      Some(new Vector2f(radius * sin(alpha).toFloat, radius * cos(alpha).toFloat).add(center))
    } else {
      None
    }

  /** Computes the angle from the begin at a given distance */
  def angle(distance: Float): Float

}
