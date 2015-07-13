package org.mmarini.scala.railways.model.blocks

import com.jme3.math.Vector2f

/**
 * @author us00852
 */
case class Transform2d(translate: Vector2f, orientation: Float) {
  /** Transforms a point */ 
  def apply(p: Vector2f): Vector2f = {
    p.clone()
    p.rotateAroundOrigin(orientation, true)
    p.addLocal(translate)
  }
}

object Transform2d {
  /** Creates a transformation */
  def apply(x: Float, y: Float, orientation: Float): Transform2d = Transform2d(new Vector2f(x, y), orientation)
}