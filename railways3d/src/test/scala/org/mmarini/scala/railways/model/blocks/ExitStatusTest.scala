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
class ExitStatusTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val track = mock[Track]
  val block = mock[ExitBlock]

  when(block.tracksForJunction).thenReturn(
    IndexedSeq(
      IndexedSeq(IndexedSeq(track))))

  val track2Group = mock[Track => Set[Track]]
  when(track2Group.apply(track)).thenReturn(Set[Track](track))

  val conf2Groups = mock[Int => Track => Set[Track]]
  when(conf2Groups.apply(0)).thenReturn(track2Group)
  when(block.trackGroupFor).thenReturn(conf2Groups)

  private val TrainId = "id"

  private def transitTrain =
    for (transit <- Arbitrary.arbitrary[Boolean])
      yield if (transit) Some(TrainId) else None

  private def create(transitTrain: Option[String]) = {
    ExitStatus(block, transitTrain)
  }

  property("tracksForJunction 0 of ExitStatus should return forward track") {
    forAll(
      (transitTrain, "transitTrain")) {
        (transitTrain: Option[String]) =>
          {
            val status = create(transitTrain)
            val tracks = status.tracksForJunction(0)
            tracks should have size (1)
            tracks should contain(track)
          }
      }
  }

  property("junctionFrom(0) should return Some(1)") {
    val status = ExitStatus(mock[ExitBlock])
    status.junctionFrom(0) shouldBe Some(1)
  }

  property("transitTrain(0) with train should return Some(train)") {
    val trainId = "train"
    ExitStatus(mock[ExitBlock], Some(trainId)).transitTrain(0) shouldBe Some(trainId)
  }

  property("transitTrain(0) should return None") {
    ExitStatus(mock[ExitBlock]).transitTrain(0) shouldBe empty
  }

  property("apply(HiddenTrack, train) should return status with transit train") {
    val x = ExitStatus(mock[ExitBlock])(0, Some("train"))
    x.transitTrain(0) shouldBe Some("train")
  }

  property("apply(otherTrack, train) should return status with none transit train") {
    val x = ExitStatus(mock[ExitBlock])(0, None)
    x.transitTrain(0) shouldBe empty
  }

  property("noTrainStatus should return status with none transit train") {
    val x = ExitStatus(mock[ExitBlock], trainId = Some("train")).noTrainStatus
    x shouldBe a[ExitStatus]
    x should have('trainId(None))
  }

  property("isClear(0) with no train should return true") {
    val x = ExitStatus(mock[ExitBlock]).isClear
    x(0) shouldBe true
  }

  property("isClear(0) with train should return false") {
    val x = ExitStatus(mock[ExitBlock], trainId = Some("train")).isClear
    x(0) shouldBe false
  }

  property("isClear(0) of locked block should return false") {
    val x = ExitStatus(mock[ExitBlock], locked = true).isClear
    x(0) shouldBe false
  }

}