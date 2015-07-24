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
import org.mmarini.scala.railways.model.tracks.HiddenTrack
import org.mmarini.scala.railways.model.tracks.Track

/** Test */
class EntryStatusTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val track = mock[Track]

  private val TrainId = "id"

  private def transitTrain =
    for (transit <- Arbitrary.arbitrary[Boolean])
      yield if (transit) Some(TrainId) else None

  private def create(transitTrain: Option[String]) = {
    val block = mock[EntryBlock]

    val f = mock[Track => (Option[Int], Option[Int])]
    when(f.apply(track)).thenReturn((None, Option(0)))

    when(block.tracksForJunction).thenReturn(
      IndexedSeq(
        IndexedSeq(
          IndexedSeq())))
    when(block.junctionsForTrack).thenReturn(IndexedSeq(f))

    val track2Group = mock[Track => Set[Track]]
    when(track2Group.apply(track)).thenReturn(Set[Track](track))

    val conf2Groups = mock[Int => Track => Set[Track]]
    when(conf2Groups.apply(0)).thenReturn(track2Group)
    when(block.trackGroupFor).thenReturn(conf2Groups)
    (block, EntryStatus(block, transitTrain))
  }

  property("tracksForJunction 0 of EntryStatus should return forward track") {
    forAll(
      (transitTrain, "transitTrain")) {
        (transitTrain: Option[String]) =>
          {
            val (block, status) = create(transitTrain)
            val tracks = status.tracksForJunction(0)
            verify(block).tracksForJunction
            tracks shouldBe empty
          }
      }
  }

  property("trackGroupFor of EntryStatus for forward track should return all tracks") {
    forAll(
      (transitTrain, "transitTrain")) {
        (transitTrain: Option[String]) =>
          {
            val (block, status) = create(transitTrain)
            val tracks = status.trackGroupFor(track)
            verify(block).trackGroupFor
            tracks should have size (1)
            tracks should contain(track)
          }
      }
  }

  property("junctionsForTrack of EntryStatus for forward track should return all tracks") {
    forAll(
      (transitTrain, "transitTrain")) {
        (transitTrain: Option[String]) =>
          {
            val (block, status) = create(transitTrain)
            val tracks = status.junctionsForTrack(track)
            verify(block).junctionsForTrack
            tracks should matchPattern {
              case (None, Some(0)) =>
            }
          }
      }
  }

  property("junctionFrom(0) should return None") {
    forAll() {
      (idx: Int) =>
        {
          val status = EntryStatus(mock[EntryBlock])
          status.junctionFrom(idx) shouldBe empty
        }
    }
  }

  property("transitTrain(0) with train should return Some(train)") {
    val trainId = "train"
    EntryStatus(mock[EntryBlock], Some(trainId)).transitTrain(0) shouldBe Some(trainId)
  }

  property("transitTrain(0) should return None") {
    EntryStatus(mock[EntryBlock]).transitTrain(0) shouldBe empty
  }

  property("apply(HiddenTrack, train) should return status with transit train") {
    val x = EntryStatus(mock[EntryBlock])(0, Some("train"))
    x.transitTrain(0) shouldBe Some("train")
  }

  property("apply(otherTrack, train) should return status with none transit train") {
    val x = EntryStatus(mock[EntryBlock])(0, None)
    x.transitTrain(0) shouldBe empty
  }

  property("noTrainStatus should return status with none transit train") {
    val x = EntryStatus(mock[EntryBlock], trainId = Some("train")).noTrainStatus
    x shouldBe a[EntryStatus]
    x should have('trainId(None))
  }
}