package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f

/**
 * @author us00852
 */
case class Transform2d(translate: Vector2f, orientation: Float) {
  /** Transforms a point */
  def apply(p: Vector2f): Vector2f = {
    val y = p.clone()
    y.rotateAroundOrigin(orientation, true)
    y.addLocal(translate)
  }

  /** Transforms an angle */
  def apply(x: Float): Float = {
    val y = x + orientation
    if (y > Pif)
      y - Pi2f
    else if (y < -Pif)
      y + Pi2f
    else y
  }
}

object Transform2d {
  /** Creates a transformation */
  def apply(x: Float, y: Float, orientation: Float): Transform2d = Transform2d(new Vector2f(x, y), orientation)
}