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

/** A factory of [[Topology]] */
object Topology {
  private val topologies = Map[String, Topology]("Litton Station" -> Litton).withDefaultValue(Litton)

  /** Returns the topology of a named station */
  def apply(id: String): Topology = topologies(id)

}
