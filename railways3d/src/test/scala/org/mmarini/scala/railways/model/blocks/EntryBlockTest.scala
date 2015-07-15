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

  property("tracksForJunction of EntryBlock from 0 should be empty") {
    block.tracksForJunction(0)(0) shouldBe empty
  }

  property("junctionRoute of EntryBlock from 0 should be empty") {
    block.junctionsForTrack(0)(HiddenTrack) should matchPattern {
      case (None, Some(0)) =>
    }
  }

  property("trackGroupFor of EntryBlock for HiddenTrack should return HiddenTrack") {
    val x = block.trackGroupFor(0)(HiddenTrack)
    x should have size (1)
    x should contain(HiddenTrack)
  }

  property("trackGroupFor of EntryBlock for other track should return empty") {
    block.trackGroupFor(0)(mock[Track]) shouldBe empty
  }

}