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

}