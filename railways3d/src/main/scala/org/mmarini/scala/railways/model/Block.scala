/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Transform
import com.jme3.math.Vector2f
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f

/**
 * A template block with geometry data
 *
 * id the block identifier
 * template the block template
 * x,y the translation vector of block
 * orientation compass rotation angle
 */
case class Block(id: String, template: BlockTemplate, x: Float, y: Float, orientation: Float)

/** Factory for [[Block]] */
object Block {

  /** Returns an entry Block */
  def entry(id: String, x: Float, y: Float, orientation: Float): Block = Block(id, Entry, x, y, orientation)

  /** Returns an exit Block */
  def exit(id: String, x: Float, y: Float, orientation: Float): Block = Block(id, Exit, x, y, orientation)

  /** Returns a platform Block */
  def platform(id: String, x: Float, y: Float, orientation: Float): Block = Block(id, Platform, x, y, orientation)

  /** Returns a platform Block */
  def segment(id: String, x: Float, y: Float, orientation: Float): Block = Block(id, Segment, x, y, orientation)

  /** Returns a platform Block */
  def leftHandSwitch(id: String, x: Float, y: Float, orientation: Float): Block = Block(id, LeftHandSwitch, x, y, orientation)

  /** */
  def rightHandSwitch(id: String, x: Float, y: Float, orientation: Float): Block = Block(id, RightHandSwitch, x, y, orientation)
}
