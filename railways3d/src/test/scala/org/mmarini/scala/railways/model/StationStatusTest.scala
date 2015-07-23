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
import org.mmarini.scala.railways.model.blocks.SegmentStatus
import org.mmarini.scala.railways.model.blocks.SegmentBlock
import sun.org.mozilla.javascript.internal.ast.Yield
import org.mmarini.scala.railways.model.blocks.SegmentStatus
import org.mmarini.scala.railways.model.blocks.SegmentStatus

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
    val status = createTest3()
    val track1 = status.blocks("track1").tracksForJunction(0)(0)
    val track2 = status.blocks("track2").tracksForJunction(0)(0)
    val x = status.apply(Set((track1, "train1"), (track2, "train2")))

    val b1 = x.blocks("track1")
    val b2 = x.blocks("track2")
    val b3 = x.blocks("track3")

    b1 shouldBe a[SegmentStatus]
    b1.asInstanceOf[SegmentStatus].trainId shouldBe (Some("train1"))

    b2 shouldBe a[SegmentStatus]
    b2.asInstanceOf[SegmentStatus].trainId shouldBe (Some("train2"))

    b3 shouldBe a[SegmentStatus]
    b3.asInstanceOf[SegmentStatus].trainId shouldBe (Some("train"))
  }

  private def createTest3() = {
    val topology = new Topology() {
      private val track1 = SegmentBlock("track1", 0f, 0f, 0f)
      private val track2 = SegmentBlock("track2", 0f, SegmentLength * 11f, 0f)
      private val track3 = SegmentBlock("track3", 0f, SegmentLength * 22f, 0f)

      val junctions = Set(
        (Endpoint(track1, 1), Endpoint(track2, 0)),
        (Endpoint(track2, 1), Endpoint(track3, 0)))

      override val viewpoints = Seq[CameraViewpoint]()
    }
    val blocks = topology.blocks
    val state = blocks.map {
      case b: SegmentBlock if (b.id == "track3") => SegmentStatus(b, Some("train")).asInstanceOf[BlockStatus]
      case b: SegmentBlock => SegmentStatus(b).asInstanceOf[BlockStatus]
    }
    StationStatus(topology, state.map(x => (x.block.id -> x)).toMap)
  }

  property("""Test the case of a station with 3 consecutive blocks
  findConnection(block1, 1) should return Some(block2, 0)""") {
    val status = createTest3()
    val block1 = status.blocks("track1")
    val block2 = status.blocks("track2")
    status.findConnection(block1)(1) shouldBe Some((block2, 0))
  }

  property("""Test the case of a station with 3 consecutive blocks
  findConnection(block2, 1) should return Some(block3, 0)""") {
    val status = createTest3()
    val block2 = status.blocks("track2")
    val block3 = status.blocks("track3")
    status.findConnection(block2)(1) shouldBe Some((block3, 0))
  }

  property("""Test the case of a station with 3 consecutive blocks
  findConnection(block3, 0) should return Some(block2, 1)""") {
    val status = createTest3()
    val block2 = status.blocks("track2")
    val block3 = status.blocks("track3")
    status.findConnection(block3)(0) shouldBe Some((block2, 1))
  }

  property("""Test the case of a station with 3 consecutive blocks
  findConnection(block3, 1) should return None""") {
    val status = createTest3()
    val block3 = status.blocks("track3")
    status.findConnection(block3)(1) shouldBe empty
  }

  property("""Test the case of a station with 3 consecutive block
  the 1st block is occupied by train 1
  the 2nd block is not occupied
  and the 3rd block by train 2
  findRoute(block, junction, trainId) should return the sequence of 1st 2 tracks.""") {

    val status = createTest3()
    val block1 = status.blocks("track1")
    val track1 = block1.block.tracksForJunction(0)(0)(0)
    val track2 = status.blocks("track2").block.tracksForJunction(0)(0)(0)
    val trainId = "train1"

    val route = status.findRoute(block1, 0, trainId)
    route should have size (2)
    route should contain(track1)
    route should contain(track2)
  }

  property("""Test the case of a station with 3 consecutive blocks
  the 1st block is occupied by train 1
  the 2nd block is not occupied
  and the 3rd block by train 2
  findRoute(train1) should return the sequence of 1st 2 tracks.""") {

    val status = createTest3()
    val block1 = status.blocks("track1")
    val track1 = block1.block.tracksForJunction(0)(0)
    val track2 = status.blocks("track2").block.tracksForJunction(0)(0)
    val location = CoachLength * 3
    val train = MovingTrain(
      "train1",
      2,
      TrainRoute(track1 ++ track2),
      location,
      0f)
    val (route, dist) = status.findRoute(train)
    dist shouldBe (location)
    route.tracks should have size (2)
    route.tracks should contain(track1(0))
    route.tracks should contain(track2(0))
  }
}