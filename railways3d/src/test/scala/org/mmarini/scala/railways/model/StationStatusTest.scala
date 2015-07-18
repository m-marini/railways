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
import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.blocks.BlockStatus
import org.mmarini.scala.railways.model.blocks.Block

/** Test */
class StationStatusTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  property(""" Test the case of a set of 2 blocks each containing 2 tracks,
  calls with a list of first track of each block
  extractBusyTrack should return all 4 tracks.""") {

    /* It would not use topology */
    val blocks1 = for { i <- 0 to 1 } yield for { j <- 0 to 1 } yield mock[Track]

    val blocks = for { (tracks, i) <- blocks1.zipWithIndex } yield {
      val block = mock[Block]
      when(block.id).thenReturn(s"Block $i")
      val st = mock[BlockStatus]
      when(st.block).thenReturn(block)
      when(st.trackGroupFor).thenReturn((t: Track) => if (tracks.contains(t)) tracks.toSet else Set[Track]())
      st
    }
    val status = StationStatus(mock[Topology], blocks.map(b => (b.block.id -> b)).toMap)

    val x = for { (tracks, i) <- blocks1.zipWithIndex } yield (tracks(0), s"Train $i")

    val exp = for {
      (tracks, i) <- blocks1.zipWithIndex
      track <- tracks
    } yield (track, s"Train $i")

    val y = status.extractBusyTrack(x.toSet)
    y should have size (4)
    for (t <- exp) {
      y should contain(t)
    }
  }

  property("apply of StationStatus should be all tracks") {
    //    val tracks = Set[(Track, String)]()
    //    val x = status.apply(tracks)
    ???
  }

  property("""Test the case of a station with 3 consecutive blocks
  findConnection(block1, 1) should return Some(block2, 0)""") {
    val track1 = mock[Track]
    val track2 = mock[Track]
    val track3 = mock[Track]

    val blocks = for { i <- 0 to 2 } yield {
      val st = mock[BlockStatus]
      (s"Block$i" -> st)
    }
    val status = StationStatus(mock[Topology], blocks.toMap)

    status.findConnection(blocks(0)._2)(1) shouldBe Some((blocks(1)._2, 0))
  }

  property("""Test the case of a station with 3 consecutive blocks
  findConnection(block2, 1) should return Some(block3, 0)""") {
    val track1 = mock[Track]
    val track2 = mock[Track]
    val track3 = mock[Track]

    val blocks = for { i <- 0 to 2 } yield {
      val st = mock[BlockStatus]
      (s"Block$i" -> st)
    }
    val status = StationStatus(mock[Topology], blocks.toMap)

    status.findConnection(blocks(1)._2)(1) shouldBe Some((blocks(2)._2, 0))
  }

  property("""Test the case of a station with 3 consecutive blocks
  findConnection(block3, 1) should return None""") {
    val track1 = mock[Track]
    val track2 = mock[Track]
    val track3 = mock[Track]

    val blocks = for { i <- 0 to 2 } yield {
      val st = mock[BlockStatus]
      (s"Block$i" -> st)
    }
    val status = StationStatus(mock[Topology], blocks.toMap)

    status.findConnection(blocks(0)._2)(1) shouldBe None
  }

  property("""Test the case of a station with 3 consecutive block
  the 1st block is occupied by train 1
  the 2nd block is not occupied
  and the 3rd block by train 2
  findRoute(block, junction, trainId) should return the sequence of 1st 2 tracks.""") {

    val trainId = "train1"
    val train1 = mock[Train]
    val train2 = mock[Train]
    val track1 = mock[Track]
    val track2 = mock[Track]
    val track3 = mock[Track]

    val blocks = for { i <- 0 to 2 } yield {
      val st = mock[BlockStatus]
      (s"Block$i" -> st)
    }
    //            when(blocks(0)._2.junctionsForTrack).thenReturn {
    //              t: Track =>
    //                if (t == track1) (Option(0), Option(1)) else (None, None)
    //            }
    val status = StationStatus(mock[Topology], blocks.toMap)
    val route = status.findRoute(blocks(0)._2, 0, trainId)
    route should have size (2)
    route should contain(track1)
    route should contain(track2)
  }

  property("""Test the case of a station with 3 consecutive blocks
  the 1st block is occupied by train 1
  the 2nd block is not occupied
  and the 3rd block by train 2
  findRoute(train1) should return the sequence of 1st 2 tracks.""") {

    ???
    //    val minTrainLength = CoachLength * 2
    //    val maxTrainLength = CoachLength * 11
    //    val maxDist = 10f
    //
    //    forAll(
    //      (Gen.chooseNum(minTrainLength, maxTrainLength), "trainLength"),
    //      (Gen.chooseNum(0f, maxDist), "location")) {
    //        (trainLength: Float,
    //        location: Float) =>
    //          {
    //            val train1 = mock[Train]
    //            val train2 = mock[Train]
    //            val track1 = mock[Track]
    //            val track2 = mock[Track]
    //            val track3 = mock[Track]
    //
    //            when(train1.trackTailLocation).thenReturn(Option((track1, location - trainLength)))
    //
    //            val blocks = for { i <- 0 to 2 } yield {
    //              val st = mock[BlockStatus]
    //              (s"Block$i" -> st)
    //            }
    //            when(blocks(0)._2.junctionsForTrack).thenReturn {
    //              t: Track =>
    //                if (t == track1) (Option(0), Option(1)) else (None, None)
    //            }
    //            val status = StationStatus(mock[Topology], blocks.toMap)
    //            val (route, dist) = status.findRoute(train1)
    //            route.tracks should have size (2)
    //            route.tracks should contain(track1)
    //            route.tracks should contain(track2)
    //
    //            dist shouldBe (location)
    //
    //          }
  }
}