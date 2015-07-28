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

  private def transitTrain =
    for (transit <- Arbitrary.arbitrary[Boolean])
      yield if (transit) Some("train") else None

  property("tracksForJunction should return forward track") {
    val status = EntryStatus(EntryBlock("", 0f, 0f, 0f), Some("train"))
    val tracks = status.tracksForJunction(0)
    tracks should have size (1)
    tracks should contain(status.block.entryTrack)
  }

  property("junctionsForTrack for forward track should return all tracks") {
    val status = EntryStatus(EntryBlock("", 0f, 0f, 0f), Some("train"))
    val tracks = status.junctionsForTrack(status.block.entryTrack)
    tracks shouldBe Some((0, 1))
  }

  property("junctionFrom(0) should return 1") {
    val status = EntryStatus(EntryBlock("", 0f, 0f, 0f))
    status.junctionFrom(0) shouldBe Some(1)
  }

  property("junctionFrom(1) should return entry") {
    val status = EntryStatus(EntryBlock("", 0f, 0f, 0f))
    status.junctionFrom(1) shouldBe empty
  }

  property("transitTrain(0) with train should return Some(train)") {
    val x = EntryStatus(EntryBlock("", 0f, 0f, 0f), Some("train")).transitTrain
    x(0) shouldBe Some("train")
  }

  property("transitTrain(0) should return None") {
    val x = EntryStatus(EntryBlock("", 0f, 0f, 0f)).transitTrain
    x(0) shouldBe empty
  }

  property("apply(0, train) should return status with transit train") {
    val x = EntryStatus(EntryBlock("", 0f, 0f, 0f))(0, Some("train"))
    x should have('trainId(Some("train")))
  }

  property("apply(0, None) should return status with none transit train") {
    val x = EntryStatus(EntryBlock("", 0f, 0f, 0f), Some("train"))(0, None)
    x should have('trainId(None))
  }

  property("noTrainStatus should return status with none transit train") {
    val x = EntryStatus(EntryBlock("", 0f, 0f, 0f), Some("train")).noTrainStatus
    x shouldBe a[EntryStatus]
    x should have('trainId(None))
  }
}