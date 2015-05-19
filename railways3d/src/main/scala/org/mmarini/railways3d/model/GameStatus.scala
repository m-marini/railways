/**
 *
 */
package org.mmarini.railways3d.model

/**
 * @author us00852
 *
 */
case class GameStatus(parms: GameParameters, topology: Topology, time: Float, blocks: Map[String, BlockStatus]) {

  /**
   *
   */
  def tick(time: Float): GameStatus = GameStatus(parms, topology, this.time + time, blocks)

}