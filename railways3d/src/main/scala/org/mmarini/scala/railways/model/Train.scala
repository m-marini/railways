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

  /** Return the train veichle compositions */
  def veichles: Option[Veichle]

}

/** */
case class IncomingTrain(id: String, entry: Block, destination: Block) extends Train {

  /** */
  def tick(time: Float, gameStatus: GameStatus): Train = {
    this
  }

  val veichles: Option[Veichle] = None
}

case class ATrain(id: String, veichles: Option[Veichle]) extends Train {
  private val Speed = new Vector2f(10f, 0)

  /** */
  def tick(time: Float, gameStatus: GameStatus): Train =
    ATrain(id, veichles.map(_.moveBy(Speed.mult(time))))
}

object ATrain {
  def apply(id: String): ATrain = ATrain(id, Some(Coach(new Vector2f, RightAngle)))
}
