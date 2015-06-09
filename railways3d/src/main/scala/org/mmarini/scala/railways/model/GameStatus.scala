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
case class GameStatus(time: Float, topology: Topology, blocks: Map[String, BlockStatus]) extends LazyLogging {

  /** Generates the next status simulating a time elapsing */
  def tick(time: Float): GameStatus = {
    val t = this.time + time
    GameStatus(t, topology, blocks)
  }

  def changeBlockStatus(id: String): GameStatus = {
    logger.debug(s"change status of $id")

    val newBlocks = blocks.
      get(id).
      map(_.changeStatus).
      map(bs => blocks + (id -> bs)).
      getOrElse(blocks)
    logger.debug(s"  $newBlocks")

    GameStatus(time, topology, newBlocks)
  }

}

/** A factory of [[GameStatus]] */
object GameStatus {
  /** Create the initial game status */
  def apply(parms: GameParameters): GameStatus = {
    val t = Topology(parms.stationName)
    val states = t.blocks.
      map(
        b => (b.id -> initialStatus(b))).
        toMap
    GameStatus(0f, t, states)
  }

  /** Returns the initial state of Block */
  private def initialStatus(block: Block): BlockStatus = block.template match {
    case Entry => EntryStatus(block)
    case Exit => ExitStatus(block, false)
    case Platform => PlatformStatus(block, false)
    case TrackTemplate => PlatformStatus(block, false)
    case LeftDeviator => DeviatorStatus(block, false, false)
    case RightDeviator => DeviatorStatus(block, false, false)
  }
}
