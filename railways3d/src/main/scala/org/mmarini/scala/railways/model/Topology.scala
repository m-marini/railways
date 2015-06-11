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
  val CameraRot = new Quaternion().fromAngleAxis(RightAngle / 9, Vector3f.UNIT_X)

  /** Returns the junctions of topology */
  def junctions: Set[Junction]

  /** Returns the blocks of the topology */
  lazy val blocks: Set[Block] = junctions.flatMap {
    case (a, b) => Set(a.block, b.block)
  }

  /** Returns the viewpoints */
  def viewpoints: Seq[CameraViewpoint]

  /** Returns the entries */
  lazy val entries: Set[Block] = blocks.filter(_.template == Entry)

  /** Returns the exit */
  lazy val exits: Set[Block] = blocks.filter(_.template == Exit)
}

case object TestStation extends Topology {

  //  private val entry = Block.entry("entry", 0f, 0f, 0f)
  //  private val exit = Block.exit("exit", 0f, 385f, StraightAngle)
  private val platform1 = Block.platform("platform1", +SegmentLength * 11 / 2, 0f, -RightAngle)
  private val platform2 = Block.platform("platform2", +SegmentLength * 11 / 2, 4, -RightAngle)
  private val platform3 = Block.platform("platform3", +SegmentLength * 11 / 2, -4, -RightAngle)

  val junctions = Set[Junction](
    (Endpoint(platform1, 0), Endpoint(platform2, 0)),
    (Endpoint(platform1, 0), Endpoint(platform3, 0)))

  /** Returns the viewpoints */
  val viewpoints: Seq[CameraViewpoint] = Seq[CameraViewpoint](
    CameraViewpoint("entry",
      new Vector3f(-SegmentLength * 11 / 2 - 10, 4.3f, 0),
      new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)),
    CameraViewpoint("exit",
      new Vector3f(SegmentLength * 11 / 2 + 10, 4.3f, 0),
      new Quaternion().fromAngleAxis(-RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)))
}

/** A factory of [[Topology]] */
object Topology {
  private val topologies = Map[String, Topology]("Downville Station" -> Downville).withDefaultValue(TestStation)

  /** Returns the topology of a named station */
  def apply(id: String): Topology = topologies(id)

}

