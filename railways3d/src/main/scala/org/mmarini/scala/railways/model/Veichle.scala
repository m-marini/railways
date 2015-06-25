/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f

/**
 * @author us00852
 *
 */
trait Veichle {
  /** Returns the location of veichle */
  def location: Vector2f

  /** Returns the orientation of veichle */
  def orientation: Float

  /** Creates a new veichle positioned by a vector from current */
  def moveBy(space: Vector2f): Veichle
}

case class Coach(location: Vector2f, orientation: Float) extends Veichle {
  /** Creates a new veichle positioned by a vector from current */
  def moveBy(space: Vector2f): Veichle = Coach(location.add(space), orientation)
}
