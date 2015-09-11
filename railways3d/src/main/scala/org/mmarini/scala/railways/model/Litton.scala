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
 *
 */
case object Litton extends Topology {

  private val P3x, P4x = -SegmentLength * 5.5f
  private val P2x = P3x - LineSwitchBlock.Length
  private val P0x, P1x = P2x - SegmentLength * 11
  private val P5x = -P3x
  private val P6x, P7x = -P2x
  private val P8x, P9x = -P0x

  private val P0y, P3y, P6y, P8y = TrackGap / 2
  private val P1y, P4y, P7y, P9y = -TrackGap / 2
  private val P2y, P5y = 0f

  private val westEntry = EntryBlock("west-entry", P0x, P0y, -RightAngle)
  private val westExit = ExitBlock("west-exit", P1x, P1y, -RightAngle)

  private val westEntryTrack = SegmentBlock("west-entry-track", P0x, P0y, RightAngle)
  private val westExitTrack = SegmentBlock("west-exit-track", P1x, P1y, RightAngle)

  private val westSwitch = LineSwitchBlock("west-switch", P2x, P2y, RightAngle)

  private val platform2 = PlatformBlock("platform2", P3x, P3y, RightAngle)
  private val platform1 = PlatformBlock("platform1", P4x, P4y, RightAngle)

  private val eastSwitch = LineSwitchBlock("east-switch", P5x, P5y, RightAngle)

  private val eastExitTrack = SegmentBlock("east-exit-track", P6x, P6y, RightAngle)
  private val eastEntryTrack = SegmentBlock("east-entry-track", P7x, P7y, RightAngle)

  private val eastExit = ExitBlock("east-exit", P8x, P8y, RightAngle)
  private val eastEntry = EntryBlock("east-entry", P9x, P9y, RightAngle)

  private val entryDev = LeftHandSwitchBlock("entry-switch", -SegmentLength * 6.5f, 0, RightAngle)
  private val exitDev = RightHandSwitchBlock("exit-switch", SegmentLength * 6.5f, 0, -RightAngle)

  private val entry = EntryBlock("entry", -SegmentLength * 17.5f, 0, -RightAngle)
  private val exit = ExitBlock("exit", SegmentLength * 17.5f, 0, RightAngle)

  private val entryTrack = SegmentBlock("entry-track", -SegmentLength * 17.5f, 0, RightAngle)
  private val exitTrack = SegmentBlock("exit-track", SegmentLength * 17.5f, 0, -RightAngle)

  val junctions = Set(
    (Endpoint(westEntry, 1), Endpoint(westEntryTrack, 0)),
    (Endpoint(westEntryTrack, 1), Endpoint(westSwitch, 0)),
    (Endpoint(westSwitch, 2), Endpoint(platform2, 0)),
    (Endpoint(platform2, 1), Endpoint(eastSwitch, 0)),
    (Endpoint(eastSwitch, 2), Endpoint(eastExitTrack, 0)),
    (Endpoint(eastExitTrack, 1), Endpoint(eastExit, 0)),

    (Endpoint(westExit, 0), Endpoint(westExitTrack, 0)),
    (Endpoint(westExitTrack, 1), Endpoint(westSwitch, 1)),
    (Endpoint(westSwitch, 3), Endpoint(platform1, 0)),
    (Endpoint(platform1, 1), Endpoint(eastSwitch, 1)),
    (Endpoint(eastSwitch, 3), Endpoint(eastEntryTrack, 0)),
    (Endpoint(eastEntryTrack, 1), Endpoint(eastEntry, 1)))

  private val YDir = tan(RightAngle / 9).toFloat
  private val LandscapeDirection = new Vector3f(0, -1, 1).normalize()
  private val West = new Vector3f(1, -YDir, 0).normalize()
  private val East = new Vector3f(-1, -YDir, 0).normalize()

  private val CameraDistance = 10f

  val viewpoints = Seq[CameraViewpoint](
    CameraViewpoint("landscape", new Vector3f(0f, 400f, -400f), LandscapeDirection),
    CameraViewpoint("entry", new Vector3f(-P0x + CameraDistance, CameraHeight, 0), East),
    CameraViewpoint("west-switching-yard", new Vector3f(-P2x + CameraDistance, CameraHeight, 0), East),
    CameraViewpoint("west-platform", new Vector3f(-P3x - CameraDistance, CameraHeight, 0), West),
    CameraViewpoint("east-platform", new Vector3f(-P5x + CameraDistance, CameraHeight, 0), East),
    CameraViewpoint("east-switching-yard", new Vector3f(-P6x - CameraDistance, CameraHeight, 0), West),
    CameraViewpoint("exit", new Vector3f(-P9x + CameraDistance, CameraHeight, 0f), East))
}
