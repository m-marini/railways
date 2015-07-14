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
import org.mmarini.scala.railways.model.tracks.Track

/** Test */
class SwitchStatusTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val forward = mock[Track]
  val backward = mock[Track]
  val divForward = mock[Track]
  val divBackward = mock[Track]
  val block = mock[SwitchBlock]

  when(block.tracksForJunction).thenReturn(
    IndexedSeq(
      IndexedSeq(
        IndexedSeq(forward),
        IndexedSeq(backward),
        IndexedSeq()),
      IndexedSeq(
        IndexedSeq(divForward),
        IndexedSeq(),
        IndexedSeq(divBackward))))

  val dirTrack2Group = mock[Track => Set[Track]]
  when(dirTrack2Group.apply(forward)).thenReturn(Set[Track](forward, backward))
  when(dirTrack2Group.apply(backward)).thenReturn(Set[Track](forward, backward))

  val divTrack2Group = mock[Track => Set[Track]]
  when(divTrack2Group.apply(divForward)).thenReturn(Set[Track](divForward, divBackward))
  when(divTrack2Group.apply(divBackward)).thenReturn(Set[Track](divForward, divBackward))

  val conf2Groups = mock[Int => Track => Set[Track]]
  when(conf2Groups.apply(0)).thenReturn(dirTrack2Group)
  when(conf2Groups.apply(1)).thenReturn(divTrack2Group)

  when(block.trackGroupFor).thenReturn(conf2Groups)

  private val TrainId = "id"

  private def transitTrain =
    for (transit <- Arbitrary.arbitrary[Boolean])
      yield if (transit) Some(TrainId) else None

  private def create(transitTrain: Option[String], locked: Boolean, diverging: Boolean) = {
    SwitchStatus(block, transitTrain, locked, diverging)
  }

  property("tracksForJunction 0 of SwitchStatus should return forward track") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked, false)
            val tracks = status.tracksForJunction(0)
            tracks should have size (1)
            tracks(0) shouldBe (forward)
          }
      }
  }

  property("tracksForJunction 1 of SwitchStatus should return backward track") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked, false)
            val tracks = status.tracksForJunction(1)
            tracks should have size (1)
            tracks(0) shouldBe (backward)
          }
      }
  }

  property("tracksForJunction 2 of SwitchStatus should return empty") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked, false)
            val tracks = status.tracksForJunction(2)
            tracks shouldBe empty
          }
      }
  }

  property("trackGroupFor of SwitchStatus for forward track should return all tracks") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked, false)
            val tracks = status.trackGroupFor(forward)
            tracks should have size (2)
            tracks should contain(forward)
            tracks should contain(backward)
          }
      }
  }

  property("trackGroupFor of SwitchStatus for backward track should return all tracks") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked, false)
            val tracks = status.trackGroupFor(backward)
            tracks should have size (2)
            tracks should contain(forward)
            tracks should contain(backward)
          }
      }
  }

  property("tracksForJunction 0 of diverging SwitchStatus should return forward track") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked, true)
            val tracks = status.tracksForJunction(0)
            tracks should have size (1)
            tracks(0) shouldBe (divForward)
          }
      }
  }

  property("tracksForJunction 1 of diverging SwitchStatus should return backward track") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked, true)
            val tracks = status.tracksForJunction(1)
            tracks shouldBe empty
          }
      }
  }

  property("tracksForJunction 2 of diverging SwitchStatus should return empty") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked, true)
            val tracks = status.tracksForJunction(2)
            tracks should have size (1)
            tracks(0) shouldBe (divBackward)
          }
      }
  }

  property("trackGroupFor of diverging SwitchStatus for forward track should return all tracks") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked, true)
            val tracks = status.trackGroupFor(divForward)
            tracks should have size (2)
            tracks should contain(divForward)
            tracks should contain(divBackward)
          }
      }
  }

  property("trackGroupFor of diverging SwitchStatus for backward track should return all tracks") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (transitTrain, "locked")) {
        (locked: Boolean, transitTrain: Option[String]) =>
          {
            val status = create(transitTrain, locked, true)
            val tracks = status.trackGroupFor(divBackward)
            tracks should have size (2)
            tracks should contain(divForward)
            tracks should contain(divBackward)
          }
      }
  }

}