/**
 *
 */
package org.mmarini.railways3d.model

import com.jme3.math.Transform
import com.jme3.math.Vector2f
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f

/** A template block with geometry data */
case class Block(id: String, template: BlockTemplate, x: Float, y: Float, rotAngle: Float)

/** Factory for [[Block]] */
object Block {

  /** Returns an entry Block */
  def entry(id: String, x: Float, y: Float, rot: Float): Block = Block(id, Entry, x, y, rot)

  /** Returns an exit Block */
  def exit(id: String, x: Float, y: Float, rot: Float): Block = Block(id, Exit, x, y, rot)

  /** Returns a platform Block */
  def platform(id: String, x: Float, y: Float, rot: Float): Block = Block(id, Platform, x, y, rot)
}