/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f

/**
 * Describes the status of a trains and creates new status of train in response of action
 */
trait Train {
  /** Returns the identifier of the train */
  def id: String

  /** Computes the next status after an elapsed time tick */
  def tick(time: Float, gameStatus: GameStatus): Train

  /** Return the train vehicle compositions */
  def vehicles: Option[Vehicle]

}

/** */
case class IncomingTrain(id: String, entry: Block, destination: Block) extends Train {

  /** */
  def tick(time: Float, gameStatus: GameStatus): Train = {
    this
  }

  val vehicles: Option[Vehicle] = None
}

/** */
case class MovingTrain(id: String, route: TrainRoute, location: Float, speed: Float) extends Train {

  /** Computes the next status after an elapsed time tick */
  def tick(time: Float, gameStatus: GameStatus): Train =
    setLocation(location + speed * time)

  /** */
  private def setLocation(location: Float) =
    MovingTrain(id, route, location, speed)

  /** Return the train vehicle compositions */
  val vehicles: Option[Vehicle] = Coach(route, location)
}
