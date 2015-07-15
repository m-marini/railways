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

  /*
   * extractBusyTrack should extend all input tracks to all tracks of groups
   * It would not use topology
   * To test creates a set of 2 blocks each containing 2 tracks and
   * calls with a list of 2 tracks of each block and a different string id
   * It should return all 4 tracks 
   */
  property("extractBusyTrack of StationStatus should be all tracks") {
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

  property("findRoute of StationStatus should be all tracks") {
    //    val train = mock[Train]
    //    val x = status.findRoute(train)
    ???
  }
}