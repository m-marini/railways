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
import org.scalacheck.Arbitrary
import org.mmarini.scala.railways.model.tracks.PlatformTrack
import org.mmarini.scala.railways.model.tracks.Track

/** Test */
class PlatformStatusTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val forward = mock[PlatformTrack]
  val backward = mock[PlatformTrack]
  val track = mock[PlatformTrack]
  val block = mock[PlatformBlock]

  when(block.tracksForJunction).thenReturn(
    IndexedSeq(
      IndexedSeq(
        IndexedSeq(forward),
        IndexedSeq(backward))))
  val track2Group = mock[Track => Set[Track]]
  when(track2Group.apply(forward)).thenReturn(Set[Track](forward, backward))
  when(track2Group.apply(backward)).thenReturn(Set[Track](forward, backward))

  val conf2Groups = mock[Int => Track => Set[Track]]
  when(conf2Groups.apply(0)).thenReturn(track2Group)
  when(block.trackGroupFor).thenReturn(conf2Groups)

  private val TrainId = "id"

  private def transitTrain =
    for (transit <- Arbitrary.arbitrary[Boolean])
      yield if (transit) Some(TrainId) else None

  private def create(transitTrain: Option[String], locked: Boolean) = {
    PlatformStatus(block, transitTrain, locked)
  }

  property("tracksForJunction 0 of PlatformStatus should return forward track") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked)
            val tracks = status.tracksForJunction(0)
            tracks should have size (1)
            tracks(0) shouldBe (forward)
          }
      }
  }

  property("tracksForJunction 1 of PlatformStatus should return backward track") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked)
            val tracks = status.tracksForJunction(1)
            tracks should have size (1)
            tracks(0) shouldBe (backward)
          }
      }
  }

  property("trackGroupFor of PlatformStatus for forward track should return all tracks") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked)
            val tracks = status.trackGroupFor(forward)
            tracks should have size (2)
            tracks should contain(forward)
            tracks should contain(backward)
          }
      }
  }

  property("trackGroupFor of PlatformStatus for backward track should return all tracks") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked)
            val tracks = status.trackGroupFor(backward)
            tracks should have size (2)
            tracks should contain(forward)
            tracks should contain(backward)
          }
      }
  }

  property("junctionFrom(0) should return Some(1)") {
    PlatformStatus(mock[PlatformBlock]).junctionFrom(0) shouldBe Some(1)
  }

  property("junctionFrom(1) should return Some(0)") {
    PlatformStatus(mock[PlatformBlock]).junctionFrom(1) shouldBe Some(0)
  }

}