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

/** Test */
class RightHandSwitchBlockTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val block = RightHandSwitchBlock("", 0, 0, 0)

  val entryPoint = Vector2f.ZERO
  val dirPoint = new Vector2f(0, SegmentLength)
  val midPoint = new Vector2f(TrackGap / 2, SegmentLength / 2)
  val divPoint = new Vector2f(TrackGap, SegmentLength)

  property("trackGroupFor of RightHandSwitchBlock for direct route should return direct track group") {
    val tg = block.trackGroups(0)
    tg should have size (1)
    tg.head should have size (2)
  }

  property("junctionRoute of RightHandSwitchBlock for direct route from 0 junction should return forward direct track") {
    block.tracksForJunction(0)(0) match {
      case (Some(1), list) =>
        list should have size (1)
        checkForSegment(list(0), entryPoint, dirPoint)
      case x => fail(s"$x not match")
    }
  }

  property("junctionRoute of RightHandSwitchBlock for direct route from 1 junction should return backward direct track") {
    block.tracksForJunction(0)(1) match {
      case (Some(0), list) =>
        list should have size (1)
        checkForSegment(list(0), dirPoint, entryPoint)
      case x => fail(s"$x not match")
    }
  }

  property("trackGroupFor of diverging RightHandSwitchBlock should return diverging track group") {
    val tg = block.trackGroups(1)
    tg should have size (1)
    tg.head should have size (4)
  }

  property("junctionRoute of diverging RightHandSwitchBlock from 0 junction should return forward diverging track") {
    block.tracksForJunction(1)(0) match {
      case (Some(2), list) =>
        list should have size (2)
        checkForRight(list(0), entryPoint, midPoint)
        checkForLeft(list(1), midPoint, divPoint)
      case x => fail(s"$x not match")
    }
  }

  property("junctionRoute of diverging RightHandSwitchBlock from 1 junction should return empty tracks") {
    block.tracksForJunction(1)(1) match {
      case (None, IndexedSeq()) =>
      case x => fail(s"$x not match")
    }
  }

  property("junctionRoute of diverging RightHandSwitchBlock from 2 junction should return backward diverging track") {
    block.tracksForJunction(1)(2) match {
      case (Some(0), list) =>
        list should have size (2)
        checkForRight(list(0), divPoint, midPoint)
        checkForLeft(list(1), midPoint, entryPoint)
      case x => fail(s"$x not match")
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