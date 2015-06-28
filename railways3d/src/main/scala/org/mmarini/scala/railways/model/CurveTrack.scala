/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math.sin
import scala.math.cos

/**
 * Describes a curve part of trajectory
 *
 * @constructor create the curve specifying the center,
 *             the radius, the starting angle and the ending angle
 *
 */

abstract class CurveTrack(center: Vector2f, radius: Float, begin: Float, length: Float) extends Track {
  assert(radius >= 0f)
  assert(length >= 0f)

  def locationAt(distance: Float): Option[Vector2f] =
    if (distance >= 0 && distance <= length) {
      val alpha = begin + angle(distance)
      Some(new Vector2f(-radius * sin(alpha).toFloat, radius * cos(alpha).toFloat).add(center))
    } else {
      None
    }

  /** */
  def angle(distance: Float): Float

}

case class RightCurveTrack(center: Vector2f, radius: Float, begin: Float, length: Float)
  extends CurveTrack(center, radius, begin, length) {
  def angle(distance: Float): Float = distance / radius
}

case class LeftCurveTrack(center: Vector2f, radius: Float, begin: Float, length: Float)
  extends CurveTrack(center, radius, begin, length) {
  def angle(distance: Float): Float = -distance / radius
}
