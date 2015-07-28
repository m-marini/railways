/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import org.mmarini.scala.railways.model.blocks.ExitBlock
import org.mmarini.scala.railways.model.blocks.Block
import org.mmarini.scala.railways.model.blocks.EntryBlock
import org.mmarini.scala.railways.model.blocks.PlatformBlock

/**
 * A set junctions and related blocks that sets up a station
 */
trait Topology {
  val CameraRot = new Quaternion().fromAngleAxis(RightAngle / 9, Vector3f.UNIT_X)

  /** Returns the junctions of topology */
  def junctions: Set[(Endpoint, Endpoint)]

  /** Returns the blocks of the topology */
  lazy val blocks: Set[Block] = {
    val set = junctions.toSet
    set.flatMap(x => Set(x._1, x._2)).map(_.block)
  }

  /** Returns the viewpoints */
  def viewpoints: Seq[CameraViewpoint]

  /** Finds the connection from the entry end point */
  lazy val findConnection: Endpoint => Option[Endpoint] =
    // Convert the junction set into a map with reverse connection
    junctions.flatMap {
      case (a, b) => Set((a -> b), (b -> a))
    }.toMap.get
}

case object TestStation extends Topology {

  //  private val entry = Block.entry("entry", 0f, 0f, 0f)
  //  private val exit = Block.exit("exit", 0f, 385f, StraightAngle)
  private val platform1 = PlatformBlock("platform1", +SegmentLength * 11f / 2, 0f, -RightAngle)
  private val platform2 = PlatformBlock("platform2", +SegmentLength * 11f / 2, 4f, -RightAngle)
  private val platform3 = PlatformBlock("platform3", +SegmentLength * 11f / 2, -4f, -RightAngle)

  val junctions = Set(
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
