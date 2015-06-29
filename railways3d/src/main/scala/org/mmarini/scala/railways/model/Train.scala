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
  def vehicles: Set[Vehicle]

}

/** */
case class IncomingTrain(id: String, entry: Block, destination: Block) extends Train {

  /** */
  def tick(time: Float, gameStatus: GameStatus): Train = {
    this
  }

  val vehicles: Set[Vehicle] = Set.empty
}

/** */
case class MovingTrain(id: String, size: Int, route: TrainRoute, location: Float, speed: Float) extends Train {

  /** Computes the next status after an elapsed time tick */
  def tick(time: Float, gameStatus: GameStatus): Train =
    setLocation(location + speed * time)

  /** */
  private def setLocation(location: Float) =
    MovingTrain(id, size, route, location, speed)

  /** Return the train vehicle compositions */
  val vehicles: Set[Vehicle] = {
    val head = Head(s"$id.head", route, location)
    val mid = for {
      i <- 1 to size - 2
      coach <- Coach(s"$id.coach.$i", route, location - i * CoachLength)
    } yield coach
    val tail = Tail(s"$id.tail", route, location - (size - 1) * CoachLength)
    (head.toSet) ++ mid ++ (tail.toSet)
  }
}
