/**
 *
 */
package org.mmarini.scala.railways.model

/**
 * Describes the status of a trains and creates new status of train in response of action
 */
trait Train {
  /** Returns the identifier of the train */
  def id: String

  /** Computes the next status after an elapsed time tick */
  def tick(time: Float, gameStatus: GameStatus): Train

}

/** */
case class IncomingTrain(id: String, entry: Block, destination: Block) extends Train {

  /** */
  def tick(time: Float, gameStatus: GameStatus): Train = {
    this
  }

}
