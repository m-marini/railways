/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f

/**
 * @author us00852
 *
 */
trait Track {

  /** */
  def length: Float

  /** */
  def locationAt(distance: Float): Option[Vector2f]
}
