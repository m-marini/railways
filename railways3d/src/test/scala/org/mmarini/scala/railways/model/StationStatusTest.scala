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
import org.mmarini.scala.railways.model.blocks.EntryBlock
import org.mmarini.scala.railways.model.blocks.ExitBlock
import org.mmarini.scala.railways.model.blocks.EntryStatus
import org.mmarini.scala.railways.model.blocks.ExitStatus

/** Test */
class StationStatusTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  private def createTest1() = {
    val topology = new Topology() {
      private val entry = EntryBlock("track1", 0f, 0f, 0f)
      private val track2 = SegmentBlock("track2", 0f, 0f, 0f)
      private val exit = ExitBlock("track3", 0f, SegmentLength * 11f, 0f)

      val junctions = Set(
        (Endpoint(entry, 0), Endpoint(track2, 0)),
        (Endpoint(track2, 1), Endpoint(exit, 0)))

      override val viewpoints = Seq[CameraViewpoint]()
    }
    val blocks = topology.blocks
    val state = for (block <- blocks) yield block match {
      case b: EntryBlock => EntryStatus(b).asInstanceOf[BlockStatus]
      case b: SegmentBlock => SegmentStatus(b).asInstanceOf[BlockStatus]
      case b: ExitBlock => ExitStatus(b).asInstanceOf[BlockStatus]
    }
    StationStatus(topology, state)
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
    StationStatus(topology, state)
  }

  property("""Test the case 1 of a station with entry, segment, exit
  apply with a trains in the entry should return a status with just 1 train
  and train route with just entry track""") {

    val status = createTest1()

    val block1 = status.blocks("track1").asInstanceOf[EntryStatus]
    val route = TrainRoute(IndexedSeq(block1.block.entryTrack))
    val train = MovingTrain(
      id = "train",
      size = 2,
      loaded = false,
      location = route.length,
      speed = 0f,
      route = route)

    val (stationStatus, trains) = status.apply(Set[Train](train))

    val b1 = stationStatus.blocks("track1")
    val b2 = stationStatus.blocks("track2")
    val b3 = stationStatus.blocks("track3")

    b1 shouldBe a[EntryStatus]
    b1.asInstanceOf[EntryStatus] should have('trainId(Some("train")))

    b2 shouldBe a[SegmentStatus]
    b2.asInstanceOf[SegmentStatus] should have('trainId(None))

    b3 shouldBe a[ExitStatus]
    b3.asInstanceOf[ExitStatus] should have('trainId(None))

    trains should have size (1)
    val routeTracks = trains.head.route.tracks

    val entryTrack = status.blocks("track1").tracksForJunction(0)(0)
    val segTrack = status.blocks("track2").tracksForJunction(0)(0)
    val exitTrack = status.blocks("track3").tracksForJunction(0)(0)

    routeTracks should have size (1)
    routeTracks should contain(entryTrack)
  }

  property("""Test the case of a station with 3 consecutive blocks
  apply with 2 trains in the first 2 blocks should return a status with just 2 trains""") {
    val status = createTest3()
    val block1 = status.blocks("track1")
    val block2 = status.blocks("track2")

    val x = status(Set(((block1, 0), "train1"), ((block2, 0), "train2")))

    val b1 = x.blocks("track1")
    val b2 = x.blocks("track2")
    val b3 = x.blocks("track3")

    b1 shouldBe a[SegmentStatus]
    b1.asInstanceOf[SegmentStatus].trainId should equal(Some("train1"))

    b2 shouldBe a[SegmentStatus]
    b2.asInstanceOf[SegmentStatus].trainId should equal(Some("train2"))

    b3 shouldBe a[SegmentStatus]
    b3.asInstanceOf[SegmentStatus].trainId should equal(None)
  }

  property("""Test the case of a station with 3 consecutive blocks
  findConnection(block1, 1) should return Some(block2, 0)""") {
    val status = createTest3()
    val block1 = status.blocks("track1")
    val block2 = status.blocks("track2")
    status.findConnection(block1)(1) should equal(Some((block2, 0)))
  }

  property("""Test the case of a station with 3 consecutive blocks
  findConnection(block2, 1) should return Some(block3, 0)""") {
    val status = createTest3()
    val block2 = status.blocks("track2")
    val block3 = status.blocks("track3")
    status.findConnection(block2)(1) should equal(Some((block3, 0)))
  }

  property("""Test the case of a station with 3 consecutive blocks
  findConnection(block3, 0) should return Some(block2, 1)""") {
    val status = createTest3()
    val block2 = status.blocks("track2")
    val block3 = status.blocks("track3")
    status.findConnection(block3)(0) should equal(Some((block2, 1)))
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
    val track1 = block1.tracksForJunction(0)(0)
    val track2 = status.blocks("track2").tracksForJunction(0)(0)
    val trainId = "train1"

    val route = status.findRoute(block1, 0, trainId, true)
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
    val track1 = block1.tracksForJunction(0)
    val track2 = status.blocks("track2").block.tracksForJunction(0)(0)
    val location = CoachLength * 3
    val train = MovingTrain(
      id = "train1",
      size = 2,
      loaded = false,
      TrainRoute(track1 ++ track2),
      location = location,
      speed = 0f)
    val (route, dist) = status.findRoute(train)
    dist should equal(location)
    route.tracks should have size (2)
    route.tracks should contain(track1(0))
    route.tracks should contain(track2(0))
  }

  property("""Test the case of a station with 3 consecutive blocks
  the 1st block is occupied by train 1
  the 2nd block is not occupied
  and the 3rd block by train 2
  extractJunction(track1) should return Some(block1, 0).""") {
    val status = createTest3()
    val block1 = status.blocks("track1")
    val track1 = block1.tracksForJunction(0)(0)
    val jun = status.extractJunctions(track1)
    jun should not be empty
    jun should matchPattern {
      case Some((block, 0)) if (block == block1) =>
    }
  }

  property("""Test the case of a station with 3 consecutive blocks
  the 1st block is occupied by train 1
  the 2nd block is not occupied
  and the 3rd block by train 2
  extractJunction(track2) should return Some(block2, 0).""") {
    val status = createTest3()
    val block2 = status.blocks("track2")
    val track2 = block2.tracksForJunction(0)(0)
    val jun = status.extractJunctions(track2)
    jun should not be empty
    jun should matchPattern {
      case Some((block, 0)) if (block == block2) =>
    }
  }

  property("""Test the case 1
  entryBlocks should return entry block.""") {
    val status = createTest1()
    val x = status.entryBlocks
    x should have size (1)
    x should contain(status.blocks("track1"))
  }

  property("""Test the case 1
  exitBlocks should return entry block.""") {
    val status = createTest1()
    val x = status.exitBlocks
    x should have size (1)
    x should contain(status.blocks("track3"))
  }

  property("""Test the case 1
  createReverseRoute should return reverse route""") {
    val status = createTest1()
    val track = status.blocks("track2").tracksForJunction(0)(0)
    val rev1 = status.blocks("track2").tracksForJunction(1)(0)
    val location = 1f
    val trainId = "train"
    val (revTrack, revLoc) = status.createReverseRoute(track, location, trainId)

    revTrack.tracks should have size (1)
    revTrack.tracks should contain(rev1)
    revLoc should equal(revTrack.length - location)
  }

}