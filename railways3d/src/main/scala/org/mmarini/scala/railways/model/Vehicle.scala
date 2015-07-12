/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math._

/**
 * @author us00852
 *
 */
trait Vehicle {
  /** Returns the id of vehicle */
  def id: String

  /** Returns the location of vehicle */
  def location: Vector2f

  /** Returns the orientation of vehicle */
  def orientation: Float

  /** Creates a new vehicle positioned by a vector from current */
  def moveBy(space: Vector2f): Vehicle
}
