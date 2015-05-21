/**
 *
 */
package org.mmarini.scala.railways.model

import com.typesafe.scalalogging.LazyLogging
import scala.util.Random

/**
 * A set of game parameter, station [[Topology]], elapsed time and set of named [[BlockStatus]]
 *
 * Generates next status by handling the incoming events
 */
case class GameStatus(time: Float, blocks: Map[String, BlockStatus]) extends LazyLogging {

  /** Generates the next status simulating a time elapsing */
  def tick(time: Float): GameStatus = {
    val t = this.time + time
    val plat = blocks("platform").asInstanceOf[PlatformStatus]
    val exit = blocks("exit").asInstanceOf[ExitStatus]

    val np = if (Random.nextDouble < time / 2) PlatformStatus(plat.block, !plat.busy) else plat
    val ne = if (Random.nextDouble < time / 1) ExitStatus(exit.block, !exit.busy) else exit
    val nb = blocks + (np.block.id -> np) + (ne.block.id -> ne)
    GameStatus(t, nb)
  }

}

/** A factory of [[GameStatus]] */
object GameStatus {
  /** Create the initial game status */
  def apply(parms: GameParameters): GameStatus = {
    val states = Topology(parms.stationName).
      blocks.
      map(
        b => (b.id -> initialStatus(b))).
        toMap
    GameStatus(0f, states)
  }

  /** Returns the initial state of Block */
  private def initialStatus(block: Block): BlockStatus = block.template match {
    case Entry => EntryStatus(block)
    case Exit => ExitStatus(block, false)
    case Platform => PlatformStatus(block, false)
  }
}
