/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import org.mmarini.scala.railways.model.blocks.ExitBlock
import org.mmarini.scala.railways.model.blocks.LeftHandSwitchBlock
import org.mmarini.scala.railways.model.blocks.RightHandSwitchBlock
import org.mmarini.scala.railways.model.blocks.SegmentBlock
import org.mmarini.scala.railways.model.blocks.EntryBlock
import org.mmarini.scala.railways.model.blocks.PlatformBlock
import scala.math.tan

/**
 * The Downville station
 */
case object Downville extends Topology {

  private val platform1 = PlatformBlock("platform1", -SegmentLength * 5.5f, 0, RightAngle)
  private val platform2 = PlatformBlock("platform2", -SegmentLength * 5.5f, TrackGap, RightAngle)

  private val entryDev = LeftHandSwitchBlock("entry-switch", -SegmentLength * 6.5f, 0, RightAngle)
  private val exitDev = RightHandSwitchBlock("exit-switch", SegmentLength * 6.5f, 0, -RightAngle)

  private val entry = EntryBlock("entry", -SegmentLength * 17.5f, 0, -RightAngle)
  private val exit = ExitBlock("exit", SegmentLength * 17.5f, 0, RightAngle)

  private val entryTrack = SegmentBlock("entry-track", -SegmentLength * 17.5f, 0, RightAngle)
  private val exitTrack = SegmentBlock("exit-track", SegmentLength * 17.5f, 0, -RightAngle)

  val junctions = Set(
    (Endpoint(entry, 1), Endpoint(entryTrack, 0)),
    (Endpoint(entryTrack, 1), Endpoint(entryDev, 0)),
    (Endpoint(entryDev, 1), Endpoint(platform1, 0)),
    (Endpoint(entryDev, 2), Endpoint(platform2, 0)),
    (Endpoint(exitDev, 1), Endpoint(platform1, 1)),
    (Endpoint(exitDev, 2), Endpoint(platform2, 1)),
    (Endpoint(exitTrack, 1), Endpoint(exitDev, 0)),
    (Endpoint(exit, 0), Endpoint(exitTrack, 0)))

  val landscapeRot = new Quaternion().fromAngleAxis(RightAngle / 2, Vector3f.UNIT_X);
  private val LandscapeDirection = new Vector3f(0, -1, 1).normalize()
  private val YDir = tan(RightAngle / 9).toFloat

  private val West = new Vector3f(1, -YDir, 0).normalize()
  private val East = new Vector3f(-1, -YDir, 0).normalize()

  private val CameraDistance = 40f

  val viewpoints = Seq[CameraViewpoint](
    CameraViewpoint("landscape", new Vector3f(0f, 400f, -400f), LandscapeDirection),
    CameraViewpoint("entry", new Vector3f(SegmentLength * 17.5f + CameraDistance, 5f, 0), East),
    CameraViewpoint("west-switching-yard", new Vector3f(SegmentLength * 6.5f + CameraDistance, 5f, TrackGap / 2), East),
    CameraViewpoint("west-platform", new Vector3f(SegmentLength * 5.5f - CameraDistance, 5f, TrackGap / 2), West),
    CameraViewpoint("east-platform", new Vector3f(-SegmentLength * 5.5f + CameraDistance, 5f, TrackGap / 2), East),
    CameraViewpoint("east-switching-yard", new Vector3f(-SegmentLength * 6.5f - CameraDistance, 5f, TrackGap / 2), West),
    CameraViewpoint("exit", new Vector3f(-SegmentLength * 17.5f + CameraDistance, 5f, 0f), East))
}
