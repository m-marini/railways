/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks
import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model._

/** Test */
class SegmentTemplateTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val seg = SegmentBlock("", 0, 0, 0)

  property("trackGroupFor of Segment should return all tracks (forward and backward)") {
    val tg = seg.trackGroups(0);
    tg should have size (1)
    tg.head should contain(SegmentTrack(Vector2f.ZERO, new Vector2f(0f, 11 * SegmentLength)))
    tg.head should contain(SegmentTrack(new Vector2f(0f, 11 * SegmentLength), Vector2f.ZERO))
  }

  property("junctionRoute of Segment from 0 should return forward track") {
    val jr0 = seg.tracksForJunction(0)(0)
    jr0 match {
      case (Some(1), list) =>
        list should have size(1)
        list should contain(SegmentTrack(Vector2f.ZERO, new Vector2f(0f, 11 * SegmentLength)))
      case _ => fail("not match")
    }
  }

  property("junctionRoute of Segment from 1 should return forward track") {
    val jr0 = seg.tracksForJunction(0)(1)
    jr0 match {
      case (Some(0), list) =>
        list should have size(1)
        list should contain(SegmentTrack(new Vector2f(0f, 11 * SegmentLength), Vector2f.ZERO))
      case _ => fail("not match")
    }
  }
}