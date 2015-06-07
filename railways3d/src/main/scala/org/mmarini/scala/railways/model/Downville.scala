/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Quaternion
import com.jme3.math.Vector3f

/**
 * The Downville station
 */
case object Downville extends Topology {

  private val platform1 = Block.platform("platform1", -SegmentLength * 11 / 2, 0f, RightAngle)
  private val platform2 = Block.platform("platform2", -SegmentLength * 11 / 2, TrackGap, RightAngle)

  private val entryDev = Block.rightDeviator("entry-dev",
    -SegmentLength * 11 / 2 - SegmentLength,
    0f,
    RightAngle)

  private val entry = Block.entry("entry",
    -SegmentLength * 11 / 2 - SegmentLength * 12,
    0f,
    -RightAngle)

  private val exitDev = Block.leftDeviator("exit-dev",
    SegmentLength * 11 / 2 + SegmentLength,
    0f,
    -RightAngle)

  private val exit = Block.exit("exit",
    SegmentLength * 11 / 2 + SegmentLength * 12,
    0f,
    RightAngle)

  private val entryTrack = Block.track("entry-track",
    -SegmentLength * 11 / 2 - SegmentLength * 12,
    0,
    RightAngle)

  private val exitTrack = Block.track("exit-track",
    SegmentLength * 11 / 2 + SegmentLength,
    0f,
    RightAngle)

  val junctions = Set(
    (Endpoint(platform1, 0), Endpoint(platform2, 0)),
    (Endpoint(entryTrack, 0), Endpoint(entryDev, 0)),
    (Endpoint(exitTrack, 0), Endpoint(exitDev, 0)),
    (Endpoint(exit, 0), Endpoint(entry, 0)))

  val viewpoints = Seq[CameraViewpoint](
    CameraViewpoint("entry1",
      new Vector3f(-SegmentLength * 11 / 2 - 10 - SegmentLength * 12, 4.3f, 0),
      new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot )),
    CameraViewpoint("entry2",
      new Vector3f(-SegmentLength * 11 / 2 + 10 - SegmentLength * 12, 4.3f, 0),
      new Quaternion().fromAngleAxis(-RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot )),
    CameraViewpoint("east-dev",
      new Vector3f(-SegmentLength * 11 / 2 + 10, 4.3f, TrackGap / 2),
      new Quaternion().fromAngleAxis(-RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot )),
    CameraViewpoint("east-platforms",
      new Vector3f(-SegmentLength * 11 / 2 - 10, 4.3f, TrackGap / 2),
      new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot )),
    CameraViewpoint("west-platforms",
      new Vector3f(SegmentLength * 11 / 2 + 10f, 4.3f, TrackGap / 2),
      new Quaternion().fromAngleAxis(-RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot )),
    CameraViewpoint("west-dev",
      new Vector3f(SegmentLength * 11 / 2 - 10f, 4.3f, TrackGap / 2),
      new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot )),
    CameraViewpoint("exit2",
      new Vector3f(SegmentLength * 11 / 2 + SegmentLength * 12 - 10f, 4.3f, 0f),
      new Quaternion().fromAngleAxis(RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot )),
    CameraViewpoint("exit1",
      new Vector3f(SegmentLength * 11 / 2 + SegmentLength * 12 + 10f, 4.3f, 0f),
      new Quaternion().fromAngleAxis(-RightAngle, Vector3f.UNIT_Y).multLocal(CameraRot )))
}
