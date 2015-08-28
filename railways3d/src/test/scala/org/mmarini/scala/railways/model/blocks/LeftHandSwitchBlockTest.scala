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
import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.tracks.LeftCurveTrack
import org.mmarini.scala.railways.model.tracks.RightCurveTrack
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model._
import org.scalacheck.Gen

/** Test */
class LeftHandSwitchBlockTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val block = LeftHandSwitchBlock("", 0, 0, 0)

  val entryPoint = Vector2f.ZERO
  val dirPoint = new Vector2f(0, SegmentLength)
  val midPoint = new Vector2f(-TrackGap / 2, SegmentLength / 2)
  val divPoint = new Vector2f(-TrackGap, SegmentLength)

  property("trackGroups of LeftHandSwitchBlock for direct route should return direct track group") {
    val tg = block.trackGroups(0)
    tg should have size (1)
    tg.head should have size (2)
  }

  property("tracksForJunction of LeftHandSwitchBlock for direct route from 0 junction should return forward direct track") {
    val list = block.tracksForJunction(0)(0)
    list should have size (1)
    checkForSegment(list(0), entryPoint, dirPoint)
  }

  property("tracksForJunction of LeftHandSwitchBlock for direct route from 1 junction should return backward direct track") {
    val list = block.tracksForJunction(0)(1)
    list should have size (1)
    checkForSegment(list(0), dirPoint, entryPoint)
  }

  property("tracksForJunction of LeftHandSwitchBlock for direct route from 2 junction should return empty") {
    block.tracksForJunction(0)(2) shouldBe empty
  }

  property("trackGroups of diverging LeftHandSwitchBlock should return diverging track group") {
    val tg = block.trackGroups(1)
    tg should have size (1)
    tg.head should have size (4)
  }

  property("tracksForJunction of diverging LeftHandSwitchBlock from 0 junction should return forward diverging track") {
    val list = block.tracksForJunction(1)(0)
    list should have size (2)
    checkForLeft(list(0), entryPoint, midPoint)
    checkForRight(list(1), midPoint, divPoint)
  }

  property("tracksForJunction of diverging LeftHandSwitchBlock from 1 junction should return empty tracks") {
    block.tracksForJunction(1)(1) shouldBe empty
  }

  property("tracksForJunction of diverging LeftHandSwitchBlock from 2 junction should return backward diverging track") {
    val list = block.tracksForJunction(1)(2)
    list should have size (2)
    checkForLeft(list(0), divPoint, midPoint)
    checkForRight(list(1), midPoint, entryPoint)
  }

  property("junctionsForTrack of LeftHandSwitchBlock should match cases") {
    val cases = Table(
      ("config", "junction", "expected"),
      (0, 0, Some((0, 1))),
      (0, 1, Some((1, 0))),
      (1, 0, Some((0, 2))),
      (1, 2, Some((2, 0))))

    forAll(cases) {
      (config: Int, junction: Int, expected: Option[(Int, Int)]) =>
        {
          val track = block.tracksForJunction(config)(junction).head
          block.junctionsForTrack(config)(track) shouldBe (expected)
        }
    }
  }

  property("trackGroupFor of LeftHandSwitchBlock from forward junction should all direct tracks") {
    val gen = for {
      config <- Gen.chooseNum(0, 1)
      track <- Gen.oneOf((0 to 2).flatMap(block.tracksForJunction(config)))
    } yield {
      (config, track)
    }

    forAll((gen, "testCase")) {
      (testCase: (Int, Track)) =>
        {
          val (config, track) = testCase
          val tracks = (0 to 2).flatMap(block.tracksForJunction(config))
          val list = block.trackGroupFor(config)(track)
          list should not be empty
          for { t <- tracks } {
            list should contain(t)
          }
        }
    }
  }

  def checkForSegment(track: Track, from: Vector2f, to: Vector2f) {
    track shouldBe a[SegmentTrack]
    checkForTrack(track, from, to)
  }

  def checkForTrack(track: Track, from: Vector2f, to: Vector2f) {
    if (from.distance(track.locationAt(0f).get) > 10e-3f) {
      track.locationAt(0f) shouldBe Some(from)
    }
    if (to.distance(track.locationAt(track.length).get) > 10e-3f) {
      track.locationAt(track.length) shouldBe Some(to)
    }
  }

  def checkForLeft(track: Track, from: Vector2f, to: Vector2f) {
    track shouldBe a[LeftCurveTrack]
    checkForTrack(track, from, to)
  }

  def checkForRight(track: Track, from: Vector2f, to: Vector2f) {
    track shouldBe a[RightCurveTrack]
    checkForTrack(track, from, to)
  }
}
