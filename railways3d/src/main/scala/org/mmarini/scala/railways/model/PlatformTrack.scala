/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import com.jme3.math.Vector3f

/**
 * Describes a linear part of trajectory
 *
 * @constructor creates the segment specifying the starting point and the ending point
 *
 */
case class PlatformTrack(begin: Vector2f, end: Vector2f) extends LinearTrack {

  /** Returns the reverse track of this */
  override def reverse :Track = new PlatformTrack(end, begin)
}
