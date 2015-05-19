/**
 *
 */
package org.mmarini.railways3d.model

/**
 * A set junctions and related blocks that sets up a station
 */
trait Topology {

  /** Returns the junctions of topology */
  def junctions: Set[Junction]

  /** Returns the blocks of the topology */
  val blocks: Set[Block] = junctions.flatMap {
    case (a, b) => Set(a.block, b.block)
  }
}

/**
 * The Downville station
 */
case object Downville extends Topology {
  private val entry = Block.entry("entry", -Platform.Length / 2, 0f, RightAngle)
  private val exit = Block.exit("exit", Platform.Length / 2, 0f, -RightAngle)
  private val platform = Block.platform("platform", -Platform.Length / 2, 0f, RightAngle)

  val junctions = Set(
    (Endpoint(entry, 0), Endpoint(platform, 0)),
    (Endpoint(exit, 0), Endpoint(platform, 1)))

} 