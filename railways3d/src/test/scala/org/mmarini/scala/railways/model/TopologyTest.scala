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
class TopologyTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  property("""Test the case of a station with 3 consecutive blocks
  block1 0 <---> 0 block2 1 <---> 0 block3
  findConnection(block1, 0) should return Some(block2, 0)""") {

    val block1 = mock[Block]
    val block2 = mock[Block]
    val block3 = mock[Block]

    val topology = new Topology() {
      val junctions = Set(
        (Endpoint(block1, 0), Endpoint(block2, 0)),
        (Endpoint(block2, 1), Endpoint(block3, 0)))

      override def viewpoints = ???
    }

    topology.findConnection(Endpoint(block1, 0)) shouldBe Some(Endpoint(block2, 0))
  }

  property("""Test the case of a station with 3 consecutive blocks
  block1 0 <---> 0 block2 1 <---> 0 block3
  findConnection(block2, 0) should return Some(block1, 0)""") {

    val block1 = mock[Block]
    val block2 = mock[Block]
    val block3 = mock[Block]

    val topology = new Topology() {
      val junctions = Set(
        (Endpoint(block1, 0), Endpoint(block2, 0)),
        (Endpoint(block2, 1), Endpoint(block3, 0)))

      override def viewpoints = ???
    }

    topology.findConnection(Endpoint(block2, 0)) shouldBe Some(Endpoint(block1, 0))
  }

  property("""Test the case of a station with 3 consecutive blocks
  block1 0 <---> 0 block2 1 <---> 0 block3
  findConnection(block2, 1) should return Some(block3, 0)""") {

    val block1 = mock[Block]
    val block2 = mock[Block]
    val block3 = mock[Block]

    val topology = new Topology() {
      val junctions = Set(
        (Endpoint(block1, 0), Endpoint(block2, 0)),
        (Endpoint(block2, 1), Endpoint(block3, 0)))

      override def viewpoints = ???
    }

    topology.findConnection(Endpoint(block2, 1)) shouldBe Some(Endpoint(block3, 0))
  }

  property("""Test the case of a station with 3 consecutive blocks
  block1 0 <---> 0 block2 1 <---> 0 block3
  findConnection(block3, 0) should return Some(block2, 1)""") {

    val block1 = mock[Block]
    val block2 = mock[Block]
    val block3 = mock[Block]

    val topology = new Topology() {
      val junctions = Set(
        (Endpoint(block1, 0), Endpoint(block2, 0)),
        (Endpoint(block2, 1), Endpoint(block3, 0)))

      override def viewpoints = ???
    }

    topology.findConnection(Endpoint(block3, 0)) shouldBe Some(Endpoint(block2, 1))
  }
}
