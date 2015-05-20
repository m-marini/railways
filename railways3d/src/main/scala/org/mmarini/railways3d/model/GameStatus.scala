/**
 *
 */
package org.mmarini.railways3d.model

/**
 * A set of game parameter, station [[Topology]], elapsed time and set of named [[BlockStatus]]
 *
 * Generates next status by handling the incoming events
 */
case class GameStatus(time: Float, blocks: Map[String, BlockStatus]) {

  /** Generates the next status simulating a time elapsing */
  def tick(time: Float): GameStatus = GameStatus(this.time + time, blocks)

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