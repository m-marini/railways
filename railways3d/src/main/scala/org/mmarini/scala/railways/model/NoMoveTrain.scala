/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math.min
import scala.math.max
import scala.math.sqrt
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model.tracks.Track

/** Describes the status of a trains and creates new status of train in response of action */
trait NoMoveTrain {

  /** Returns the speed of train */
  def speed: Float = 0
}
