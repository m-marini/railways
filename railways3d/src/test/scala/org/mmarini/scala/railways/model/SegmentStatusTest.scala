/**
 *
 */
package org.mmarini.scala.railways.model

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks
import com.jme3.math.Vector2f
import org.scalacheck.Gen
import ModelGen._
import org.scalacheck.Arbitrary

/** Test */
class SegmentStatusTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  property("trackGroupFor of SegmentStatus should return all tracks (forward and backward)") {
    val track1 = mock[Track]
    val track_1 = mock[Track]
    val track2 = mock[Track]
    val track_2 = mock[Track]

    when(track1.reverse) thenReturn (track_1)
    when(track_1.reverse) thenReturn (track1)
    when(track2.reverse) thenReturn (track_2)
    when(track_2.reverse) thenReturn (track2)

    forAll(
      (Arbitrary.arbitrary[Boolean], "busy"),
      (Arbitrary.arbitrary[Boolean], "locked")) {
        (busy: Boolean, locked: Boolean) =>
          {
            val block = mock[Block]
            val seg = SegmentStatus(block: Block, busy, locked)
            val trackGroup = seg.trackGroupFor(track1)
            trackGroup should have size (4)
            trackGroup should contain allOf (track1, track2, track_1, track_2)
          }
      }
  }
}