/**
 *
 */
package org.mmarini.railways3d.model

import com.jme3.math.Transform
import com.jme3.math.Vector2f
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f

/**
 * @author us00852
 *
 */
case class Block(name: String, template: BlockTemplate, transform: Transform) {

}

object Block {
  def apply(name: String, template: BlockTemplate, x: Float, y: Float, rot: Float): Block = {
    val transform = new Transform()
    transform.setTranslation(x, y, 0f)
    //    transform.setRotation()
    val q = new Quaternion().fromAngleAxis(rot, new Vector3f(0f, 0f, 1f))
    transform.setRotation(q)
    Block(name, template, transform)
  }
}