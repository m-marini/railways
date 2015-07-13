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
    (Endpoint(platform1, 0), Endpoint(platform2, 0)),
    (Endpoint(entryTrack, 0), Endpoint(entryDev, 0)),
    (Endpoint(exitTrack, 0), Endpoint(exitDev, 0)),
    (Endpoint(exit, 0), Endpoint(entry, 0)))

  val landscapeRot = new Quaternion().fromAngleAxis(RightAngle / 2, Vector3f.UNIT_X);

  val viewpoints = Seq[CameraViewpoint](
    CameraViewpoint("landscape",
      new Vector3f(0f, 400f, -400f),
      new Quaternion().fromAngleAxis(0f, Vector3f.UNIT_Y).multLocal(landscapeRot)),
    CameraViewpoint("entry1",
      new Vector3f(SegmentLength * 17.5f - 10, 5f, 0),
      new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)),
    CameraViewpoint("entry2",
      new Vector3f(SegmentLength * 17.5f + 10, 5f, 0),
      new Quaternion().fromAngleAxis(-RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)),
    CameraViewpoint("east-switch1",
      new Vector3f(SegmentLength * 6.5f + 10, 5f, TrackGap / 2),
      new Quaternion().fromAngleAxis(-RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)),
    CameraViewpoint("east-switch2",
      new Vector3f(SegmentLength * 5.5f - 10, 5f, TrackGap / 2),
      new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)),
    CameraViewpoint("east-platforms",
      new Vector3f(SegmentLength * 11 / 2 + 10, 5f, TrackGap / 2),
      new Quaternion().fromAngleAxis(-RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)),
    CameraViewpoint("west-platforms",
      new Vector3f(-SegmentLength * 5.5f - 10, 5f, TrackGap / 2),
      new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)),
    CameraViewpoint("west-switch2",
      new Vector3f(-SegmentLength * 5.5f + 10, 5f, TrackGap / 2),
      new Quaternion().fromAngleAxis(-RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)),
    CameraViewpoint("west-switch1",
      new Vector3f(-SegmentLength * 6.5f - 10, 5f, TrackGap / 2),
      new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)),
    CameraViewpoint("exit2",
      new Vector3f(-SegmentLength * 17.5f + 10, 5f, 0f),
      new Quaternion().fromAngleAxis(-RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)),
    CameraViewpoint("exit1",
      new Vector3f(-SegmentLength * 17.5f - 10, 5f, 0f),
      new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot)))
}
