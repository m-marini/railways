/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math.min
import scala.math.max
import scala.math.sqrt
import com.typesafe.scalalogging.LazyLogging

/** This StoppedTrain computes the next state of this stopped train */
case class StoppedTrain(
    id: String,
    size: Int,
    route: TrainRoute,
    location: Float) extends Train {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float): Train = StoppedTrain(id, size, route, location)

  /** Computes the next status after an elapsed time tick */
  def tick(time: Float, gameStatus: GameStatus): Option[Train] = Some(this)
}
