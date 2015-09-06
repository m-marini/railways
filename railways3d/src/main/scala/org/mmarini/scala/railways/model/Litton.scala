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
import org.mmarini.scala.railways.model.blocks.LineSwitchBlock

/**
 * The Litton station
 * 
 * P0 ---        P3 ---    --- P6 --- P8
 *        P2 ---        P5      
 * P1 ---        P4 ---    --- P7 --- P9
 */
case object Litton extends Topology {

  
  private val platform1 = PlatformBlock("platform1", -SegmentLength * 5.5f, TrackGap / 2, RightAngle)
  private val platform2 = PlatformBlock("platform2", -SegmentLength * 5.5f, -TrackGap / 2, RightAngle)

  private val westEntry = EntryBlock("west-entry", -SegmentLength * 17.5f, TrackGap / 2, -RightAngle)
  private val westExit = ExitBlock("west-exit", -SegmentLength * 17.5f, TrackGap / 2, -RightAngle)
  private val eastEntry = EntryBlock("east-entry", SegmentLength * 17.5f, TrackGap / 2, RightAngle)
  private val eastExit = ExitBlock("east-exit", SegmentLength * 17.5f, TrackGap / 2, RightAngle)

  private val westEntryTrack = SegmentBlock("west-entry-track", -SegmentLength * 17.5f, 0, RightAngle)
  private val westExitTrack = SegmentBlock("west-exit-track", -SegmentLength * 17.5f, 0, RightAngle)
  private val eastEntryTrack = SegmentBlock("east-entry-track", SegmentLength * 17.5f, 0, -RightAngle)
  private val eastExitTrack = SegmentBlock("east-exit-track", SegmentLength * 17.5f, 0, -RightAngle)

  private val westSwitch = LineSwitchBlock("west-switch", 0f, 0f, 0f)
  private val eastSwitch = LineSwitchBlock("east-switch", 0f, 0f, 0f)

  val junctions = Set(
    (Endpoint(westEntry, 1), Endpoint(westEntryTrack, 0)),
    (Endpoint(westEntryTrack, 1), Endpoint(westSwitch, 0)),
    (Endpoint(westSwitch, 2), Endpoint(platform1, 0)),
    (Endpoint(platform1, 1), Endpoint(eastSwitch, 0)),
    (Endpoint(eastSwitch, 2), Endpoint(eastExitTrack, 0)),
    (Endpoint(eastExitTrack, 1), Endpoint(eastExit, 0)))

  val landscapeRot = new Quaternion().fromAngleAxis(RightAngle / 2, Vector3f.UNIT_X);
  private val LandscapeDirection = new Vector3f(0, -1, 1).normalize()
  private val YDir = tan(RightAngle / 9).toFloat

  private val West = new Vector3f(1, -YDir, 0).normalize()
  private val East = new Vector3f(-1, -YDir, 0).normalize()

  private val CameraDistance = 10f

  val viewpoints = Seq[CameraViewpoint](
    CameraViewpoint("landscape", new Vector3f(0f, 400f, -400f), LandscapeDirection),
    CameraViewpoint("entry", new Vector3f(SegmentLength * 17.5f + CameraDistance, 5f, 0), East),
    CameraViewpoint("west-switching-yard", new Vector3f(SegmentLength * 6.5f + CameraDistance, 5f, TrackGap / 2), East),
    CameraViewpoint("west-platform", new Vector3f(SegmentLength * 5.5f - CameraDistance, 5f, TrackGap / 2), West),
    CameraViewpoint("east-platform", new Vector3f(-SegmentLength * 5.5f + CameraDistance, 5f, TrackGap / 2), East),
    CameraViewpoint("east-switching-yard", new Vector3f(-SegmentLength * 6.5f - CameraDistance, 5f, TrackGap / 2), West),
    CameraViewpoint("exit", new Vector3f(-SegmentLength * 17.5f + CameraDistance, 5f, 0f), East))
}
