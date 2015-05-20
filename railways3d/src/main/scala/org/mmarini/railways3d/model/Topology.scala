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
  lazy val blocks: Set[Block] = junctions.flatMap {
    case (a, b) => Set(a.block, b.block)
  }
}

/**
 * The Downville station
 */
case object TestStation extends Topology {
  private val entry = Block.entry("entry", 0f, 0f, 0f)
  private val exit = Block.exit("exit", 0f, 385f, StraightAngle)
  private val platform = Block.platform("platform", 0f, 0f, StraightAngle)

  val junctions = Set(
    (Endpoint(entry, 0), Endpoint(platform, 0)),
    (Endpoint(exit, 0), Endpoint(platform, 1)))
}

case object Downville extends Topology {
  private val entry = Block.entry("entry", -Platform.Length / 2, 0f, -RightAngle)
  private val exit = Block.exit("exit", Platform.Length / 2, 0f, RightAngle)
  private val platform = Block.platform("platform", -Platform.Length / 2, 0f, RightAngle)

  val junctions = Set(
    (Endpoint(entry, 0), Endpoint(platform, 0)),
    (Endpoint(exit, 0), Endpoint(platform, 1)))
}

/** A factory of [[Topology]] */
object Topology {
  private val topologies = Map[String, Topology]()

  /** Returns the topology of a named station */
  def apply(id: String): Topology = topologies.getOrElse(id, Downville)
}

