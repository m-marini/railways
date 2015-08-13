/**
 *
 */
package org.mmarini.scala.railways

/**
 * @author us00852
 *
 */

import scala.math.Pi
import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.blocks.Block

/** */
package object model {
  val BoardingTime = 60f

  val Pif = Pi.toFloat
  val Pi2f = (2 * Pi).toFloat

  val RightAngle = (Pi / 2).toFloat
  val StraightAngle = Pif
  val FullAngle = Pi2f

  /** Coach length   */
  val CoachLength = 25f

  /** Coach width  */
  val CoachWidth = 3f;

  /** Track width */
  val TrackWidth = 1.435f

  /** Max train size */
  val MinTrainSize = 7

  /** Max train size */
  val MaxTrainSize = 11

  /** Segment * (1-cos(curve))/sin(curve) */
  val TrackGap = 4.608f

  /** Length units of standard segment element (meters) */
  val SegmentLength = 35f

  /** Minimum distance to stop the train at (meters) */
  val MinDistance = 0.01f

  /** Radius of curve */
  val CurveRadius = 67.615f

  /** Curve angle */
  val CurveAngle = RightAngle / 3f;

  val CurveLength = CurveAngle * CurveRadius

  /** Maximum speed */
  val MaxSpeed = 140f / 3.6f

  /** Minimum speed */
  val MinSpeed = 0.2f

  /** Max deceleration */
  val MaxDeceleration = 1.929f

  /** Max acceleration */
  val MaxAcceleration = 0.623053f

  val AxisDistance = 3.970f

  case class Endpoint(block: Block, index: Int)

}
