/**
 *
 */
package org.mmarini.railways3d

/**
 * @author us00852
 *
 */

import scala.math.Pi
import com.jme3.math.Vector2f

package object model {
  val Pif = Pi.toFloat
  val Pi2f = (2 * Pi).toFloat

  /**
   * Coach length
   */
  val CoachLength = 25f

  /**
   * Coach width
   */
  val CoachWidth = 3f;

  /**
   * Track width
   */
  val TrackWidth = 1.435f

  // Segment * (1-cos(curve))/sin(curve) 
  val TrackGap = 4.608f

  /**
   * Length units of standard segment element (meters)
   */
  val SegmentLength = 35f

  type Path = IndexedSeq[Track]
  type Endpoint = (Block, Int)
  type Junction = (Endpoint, Endpoint)
  type GameTransition = GameStatus => GameStatus

}