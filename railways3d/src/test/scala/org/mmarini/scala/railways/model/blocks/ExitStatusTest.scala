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

  property("trackGroupFor of ExitStatus for forward track should return all tracks") {
    forAll(
      (transitTrain, "transitTrain")) {
        (transitTrain: Option[String]) =>
          {
            val status = create(transitTrain)
            val tracks = status.trackGroupFor(track)
            tracks should have size (1)
            tracks should contain(track)
          }
      }
  }

}