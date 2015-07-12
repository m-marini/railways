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

/** Test */
class StationStatusTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  property("extractBusyTrack of StationStatu should be all tracks") {
    val topology = mock[Topology]

    val blockMap = Map.empty[String, BlockStatus]
    val status = StationStatus(topology, blockMap)
    val train1 = mock[Train]
    val train2 = mock[Train]
    val track11 = mock[Track]
    val track12 = mock[Track]
    val track21 = mock[Track]
    val track22 = mock[Track]

    val trainTracks = Set((track11, train1), (track12, train1), (track21, train2), (track22, train2))
    val bt = status.extractBusyTrack(trainTracks)
    bt should have size (6)
  }
}