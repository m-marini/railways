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
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model.tracks.Track

/** Test */
class SegmentStatusTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val forward = mock[SegmentTrack]
  val backward = mock[SegmentTrack]
  val track = mock[SegmentTrack]
  val block = mock[SegmentBlock]

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
    SegmentStatus(block, transitTrain, locked)
  }

  property("tracksForJunction 0 of SegmentStatus should return forward track") {
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

  property("tracksForJunction 1 of SegmentStatus should return backward track") {
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

  property("trackGroupFor of SegmentStatus for forward track should return all tracks") {
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

  property("trackGroupFor of SegmentStatus for backward track should return all tracks") {
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
    SegmentStatus(mock[SegmentBlock]).junctionFrom(0) shouldBe Some(1)
  }

  property("junctionFrom(1) should return Some(0)") {
    SegmentStatus(mock[SegmentBlock]).junctionFrom(1) shouldBe Some(0)
  }

  property("transitTrain(0) in case of train should return Some(trainId)") {
    val trainId = "train"
    SegmentStatus(mock[SegmentBlock], Some(trainId)).transitTrain(0) shouldBe Some(trainId)
  }

  property("transitTrain(1) in case of train should return Some(trainId)") {
    val trainId = "train"
    SegmentStatus(mock[SegmentBlock], Some(trainId)).transitTrain(1) shouldBe Some(trainId)
  }

  property("transitTrain(0) should return None") {
    SegmentStatus(mock[SegmentBlock]).transitTrain(0) shouldBe empty
  }

  property("transitTrain(1) should return None") {
    SegmentStatus(mock[SegmentBlock]).transitTrain(1) shouldBe empty
  }

  property("apply(0, train) should return status with transit train") {
    val x = SegmentStatus(mock[SegmentBlock])(0, Some("train"))
    x.transitTrain(0) shouldBe Some("train")
    x.transitTrain(1) shouldBe Some("train")
  }

  property("apply(1, train) should return status with transit train") {
    val x = SegmentStatus(mock[SegmentBlock])(1, Some("train"))
    x.transitTrain(0) shouldBe Some("train")
    x.transitTrain(1) shouldBe Some("train")
  }

  property("apply(0, none) should return status with none transit train") {
    val x = SegmentStatus(mock[SegmentBlock])(0, None)
    x.transitTrain(0) shouldBe empty
    x.transitTrain(1) shouldBe empty
  }

  property("noTrainStatus should return status with none transit train") {
    val x = SegmentStatus(mock[SegmentBlock], trainId = Some("train")).noTrainStatus
    x shouldBe a[SegmentStatus]
    x should have('trainId(None))
  }

}