/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f
import scala.math.min
import scala.math.max
import scala.math.sqrt
import com.typesafe.scalalogging.LazyLogging

/** This WaitForPassengerTrain computes the next state of this train that is waiting for passenger boarding */
case class WaitForPassengerTrain(
  id: String,
  size: Int,
  route: TrainRoute,
  location: Float,
  timeout: Float,
  exitId: String) extends Train
    with NoMoveTrain
    with LazyLogging {

  /** Creates the new train status apply a new route */
  override def apply(route: TrainRoute, location: Float): Train = WaitForPassengerTrain(id, size, route, location, timeout, exitId)

  /** Computes the next status after an elapsed time tick */
  override def tick(time: Float, gameStatus: GameStatus): (Option[Train], Seq[TrainMessage]) =
    if (timeout - time > 0) {
      (Some(
        WaitForPassengerTrain(id, size, route, location, timeout - time, exitId)),
        Seq())
    } else {
      logger.debug(s"$id stopped")
      (Some(
        StoppedTrain(id, size, true, route, location, exitId)),
        Seq(TrainReloadedMsg(id)))
    }

  /** Returns true if train has loaded passengers at platform */
  override def loaded: Boolean = true
}
