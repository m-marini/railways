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
import org.mmarini.scala.railways.model.tracks.HiddenTrack
import org.mmarini.scala.railways.model.tracks.Track

/** Test */
class EntryBlockTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val block = EntryBlock("", 0, 0, 0)

  property("tracksForJunction of EntryBlock from 0 should hidden track") {
    val x = block.tracksForJunction(0)(0)
    x should have size (1)
    x should contain(block.entryTrack)
  }

  property("junctionsForTrack of EntryBlock from 0 should Some(0, 1)") {
    block.junctionsForTrack(0)(block.entryTrack) shouldBe (Some(0, 1))
  }

  property("trackGroupFor of EntryBlock for HiddenTrack should return HiddenTrack") {
    val x = block.trackGroupFor(0)(block.entryTrack)
    x should have size (1)
    x should contain(block.entryTrack)
  }

  property("trackGroupFor of EntryBlock for other track should return empty") {
    block.trackGroupFor(0)(mock[Track]) shouldBe empty
  }

}