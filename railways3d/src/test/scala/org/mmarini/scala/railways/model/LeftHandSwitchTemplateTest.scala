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
import scala.collection.immutable.Vector

/** Test */
class LefHandSwitchTemplateTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val seg = LeftHandSwitchTemplate

  val entryPoint = Vector2f.ZERO
  val dirPoint = new Vector2f(0, SegmentLength)
  val midPoint = new Vector2f(-TrackGap / 2, SegmentLength / 2)
  val divPoint = new Vector2f(-TrackGap, SegmentLength)

  property("trackGroupFor of LefHandSwitchTemplate for direct route should return direct track group") {
    val tg = seg.trackGroups(0);
    tg should have size (1)
    //    tg should contain(dirFor)
    //    tg should contain(dirBack)
  }

  property("junctionRoute of LefHandSwitchTemplate for direct route from 0 junction should return forward direct track") {
    val jr0 = seg.junctionRoute(0)(0)
    jr0 should not be empty
    jr0 match {
      case Some((1, list)) =>
        list should have size (1)
        checkForSegment(list(0), entryPoint, dirPoint)
      case _ => fail("not match")
    }
  }

  property("junctionRoute of LefHandSwitchTemplate for direct route from 1 junction should return backward direct track") {
    val jr0 = seg.junctionRoute(0)(1)
    jr0 should not be empty
    jr0 match {
      case Some((0, list)) =>
        list should have size (1)
        checkForSegment(list(0), dirPoint, entryPoint)
      case _ => fail("not match")
    }
  }

  property("trackGroupFor of diverging LefHandSwitchTemple should return diverging track group") {
    val tg = seg.trackGroups(1);
    tg should have size (1)
    //    tg should contain(dirFor)
    //    tg should contain(dirBack)
  }

  property("junctionRoute of diverging LefHandSwitchTemplate from 0 junction should return forward diverging track") {
    val jr0 = seg.junctionRoute(1)(0)
    jr0 should not be empty
    jr0 match {
      case Some((2, list)) =>
        list should have size (2)
        checkForLeft(list(0), entryPoint, midPoint)
        checkForRight(list(1), midPoint, divPoint)
      case _ => fail("not match")
    }
  }

  property("junctionRoute of diverging LefHandSwitchTemplate from 1 junction should return empty tracks") {
    val jr0 = seg.junctionRoute(1)(1)
    jr0 shouldBe empty
  }

  property("junctionRoute of diverging LefHandSwitchTemplate from 2 junction should return backward diverging track") {
    val jr0 = seg.junctionRoute(1)(2)
    jr0 should not be empty
    jr0 match {
      case Some((0, list)) =>
        list should have size (2)
        checkForLeft(list(0), divPoint, midPoint)
        checkForRight(list(1), midPoint, entryPoint)
      case _ => fail("not match")
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