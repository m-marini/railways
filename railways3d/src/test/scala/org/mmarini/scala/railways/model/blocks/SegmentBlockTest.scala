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
  val block = SegmentBlock("", 0, 0, 0)

  property("trackGroups of Segment should return all tracks (forward and backward)") {
    val tg = block.trackGroups(0);
    tg should have size (1)
    tg.head should contain(SegmentTrack(Vector2f.ZERO, new Vector2f(0f, 11 * SegmentLength)))
    tg.head should contain(SegmentTrack(new Vector2f(0f, 11 * SegmentLength), Vector2f.ZERO))
  }

  property("tracksForJunction of Segment from 0 should return forward track") {
    val list = block.tracksForJunction(0)(0)
    list should have size (1)
    list should contain(SegmentTrack(Vector2f.ZERO, new Vector2f(0f, 11 * SegmentLength)))
  }

  property("tracksForJunction of Segment from 1 should return forward track") {
    val list = block.tracksForJunction(0)(1)
    list should have size (1)
    list should contain(SegmentTrack(new Vector2f(0f, 11 * SegmentLength), Vector2f.ZERO))
  }

  property("junctionsForTrack forward of PlatformBlock should be 0 - 1") {
    val track = block.tracksForJunction(0)(0)(0)
    val x = block.junctionsForTrack(0)(track)
    x shouldBe Some((0, 1))
  }

  property("junctionsForTrack backward of PlatformBlock should be 1 - 0") {
    val track = block.tracksForJunction(0)(1)(0)
    val x = block.junctionsForTrack(0)(track)
    x shouldBe Some((1, 0))
  }

  property("trackGroupFor forward of PlatformBlock should return all tracks") {
    val forward = block.tracksForJunction(0)(0)(0)
    val backward = block.tracksForJunction(0)(1)(0)
    val x = block.trackGroupFor(0)(forward)
    x should have size (2)
    x should contain(forward)
    x should contain(backward)
  }

  property("trackGroupFor backward of PlatformBlock should return all tracks") {
    val forward = block.tracksForJunction(0)(0)(0)
    val backward = block.tracksForJunction(0)(1)(0)
    val x = block.trackGroupFor(0)(backward)
    x should have size (2)
    x should contain(forward)
    x should contain(backward)
  }

}
