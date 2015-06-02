/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Quaternion
import com.jme3.math.Vector3f

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

  /** Returns the viewpoints */
  def viewpoints: Seq[CameraViewpoint]
}
case object TestStation extends Topology {
  private val entry = Block.entry("entry", 0f, 0f, 0f)
  private val exit = Block.exit("exit", 0f, 385f, StraightAngle)
  private val platform = Block.platform("platform", 0f, 0f, StraightAngle)

  val junctions = Set[Junction]( //    (Endpoint(entry, 0), Endpoint(platform, 0)),
  //    (Endpoint(exit, 0), Endpoint(platform, 1)))
  )

  /** Returns the viewpoints */
  val viewpoints: Seq[CameraViewpoint] = Nil
}

/** A factory of [[Topology]] */
object Topology {
  private val topologies = Map[String, Topology]().withDefaultValue(Downville)

  /** Returns the topology of a named station */
  def apply(id: String): Topology = topologies(id)

}

