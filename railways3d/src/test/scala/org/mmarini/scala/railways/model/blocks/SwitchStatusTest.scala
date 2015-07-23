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
import org.scalacheck.Gen

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

  property("junctionFrom(0) in case of direct switch should return Some(1)") {
    SwitchStatus(mock[SwitchBlock], None, false, false).junctionFrom(0) shouldBe Some(1)
  }

  property("junctionFrom(1) in case of direct switch should return Some(0)") {
    SwitchStatus(mock[SwitchBlock], None, false, false).junctionFrom(1) shouldBe Some(0)
  }

  property("junctionFrom(2) in case of direct switch should return None") {
    SwitchStatus(mock[SwitchBlock], None, false, false).junctionFrom(2) shouldBe empty
  }

  property("junctionFrom(0) in case of diverging switch should return Some(2)") {
    SwitchStatus(mock[SwitchBlock], None, false, true).junctionFrom(0) shouldBe Some(2)
  }

  property("junctionFrom(1) in case of diverging switch should return None") {
    SwitchStatus(mock[SwitchBlock], None, false, true).junctionFrom(1) shouldBe empty
  }

  property("junctionFrom(2) in case of diverging switch should return Some(0)") {
    SwitchStatus(mock[SwitchBlock], None, false, true).junctionFrom(2) shouldBe Some(0)
  }

  property("transitTrain should return None") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (Arbitrary.arbitrary[Boolean], "diverging"),
      (Gen.oneOf(0, 1, 2), "junction")) {
        (locked: Boolean,
        diverging: Boolean,
        junction: Int) =>
          {
            SwitchStatus(mock[SwitchBlock], None, locked, diverging).transitTrain(junction) shouldBe empty
          }
      }
  }

  property("transitTrain should return Some(train)") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (Gen.oneOf(0, 1), "junction")) {
        (locked: Boolean,
        junction: Int) =>
          {
            SwitchStatus(mock[SwitchBlock], Some("train"), locked, false).transitTrain(junction) shouldBe Some("train")
          }
      }
  }

  property("transitTrain diverging should return Some(train)") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked"),
      (Gen.oneOf(0, 2), "junction")) {
        (locked: Boolean,
        junction: Int) =>
          {
            SwitchStatus(mock[SwitchBlock], Some("train"), locked, true).transitTrain(junction) shouldBe Some("train")
          }
      }
  }

  property("transitTrain(2) should return None") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked")) {
        (locked: Boolean) =>
          {
            SwitchStatus(mock[SwitchBlock], Some("train"), locked, false).transitTrain(2) shouldBe empty
          }
      }
  }

  property("transitTrain(1) diverging should return None") {
    forAll(
      (Arbitrary.arbitrary[Boolean], "locked")) {
        (locked: Boolean) =>
          {
            SwitchStatus(mock[SwitchBlock], Some("train"), locked, true).transitTrain(1) shouldBe empty
          }
      }
  }

  property("apply(x, train) should return status with transit train") {
    forAll(
      (Gen.oneOf(0, 1), "junction")) {
        (junction: Int) =>
          {
            val x = SwitchStatus(mock[SwitchBlock])(junction, Some("train"))
            x.transitTrain(0) shouldBe Some("train")
            x.transitTrain(1) shouldBe Some("train")
            x.transitTrain(2) shouldBe None
          }
      }
  }

  property("apply(2, train) should return status with no transit train") {
    val x = SwitchStatus(mock[SwitchBlock])(2, Some("train"))
    x.transitTrain(0) shouldBe None
    x.transitTrain(1) shouldBe None
    x.transitTrain(2) shouldBe None
  }

  property("apply(x, None) with train should return status with no transit train") {
    forAll(
      (Gen.oneOf(0, 1), "junction")) {
        (junction: Int) =>
          {
            val x = SwitchStatus(mock[SwitchBlock], Some("train"))(junction, None)
            x.transitTrain(0) shouldBe None
            x.transitTrain(1) shouldBe None
            x.transitTrain(2) shouldBe None
          }
      }
  }

  property("apply(2, None) should return status with transit train") {
    val x = SwitchStatus(mock[SwitchBlock], Some("train"))(2, None)
    x.transitTrain(0) shouldBe Some("train")
    x.transitTrain(1) shouldBe Some("train")
    x.transitTrain(2) shouldBe None
  }

  property("apply(x, train) diverging should return status with transit train") {
    forAll(
      (Gen.oneOf(0, 2), "junction")) {
        (junction: Int) =>
          {
            val x = SwitchStatus(mock[SwitchBlock], diverging = true)(junction, Some("train"))
            x.transitTrain(0) shouldBe Some("train")
            x.transitTrain(1) shouldBe None
            x.transitTrain(2) shouldBe Some("train")
          }
      }
  }

  property("apply(1, train) diverging should return status with no transit train") {
    val x = SwitchStatus(mock[SwitchBlock], diverging = true)(1, Some("train"))
    x.transitTrain(0) shouldBe None
    x.transitTrain(1) shouldBe None
    x.transitTrain(2) shouldBe None
  }

  property("apply(x, None) diverging with train should return status with no transit train") {
    forAll(
      (Gen.oneOf(0, 2), "junction")) {
        (junction: Int) =>
          {
            val x = SwitchStatus(mock[SwitchBlock], Some("train"), diverging = true)(junction, None)
            x.transitTrain(0) shouldBe None
            x.transitTrain(1) shouldBe None
            x.transitTrain(2) shouldBe None
          }
      }
  }

  property("apply(1, None) diverging with train should return status with transit train") {
    val x = SwitchStatus(mock[SwitchBlock], Some("train"), diverging = true)(1, None)
    x.transitTrain(0) shouldBe Some("train")
    x.transitTrain(1) shouldBe None
    x.transitTrain(2) shouldBe Some("train")
  }
}