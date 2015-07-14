/**
 *
 */
package org.mmarini.scala.railways.model.tracks

import com.jme3.math.Vector2f

/**
 * @author us00852
 *
 */
trait Track {

  /** Returns the length of this track */
  def length: Float

  /** Returns the point of this track at a give distance from the begin */
  def locationAt(distance: Float): Option[Vector2f]
}