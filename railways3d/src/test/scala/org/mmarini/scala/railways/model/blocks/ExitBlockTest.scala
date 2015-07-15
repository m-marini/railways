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
class ExitBlockTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val block = ExitBlock("", 0, 0, 0)

  property("tracksForJunction of ExitBlock from 0 should be empty") {
    block.tracksForJunction(0)(0) should have size (1)
    block.tracksForJunction(0)(0) should contain(HiddenTrack)
  }

  property("junctionRoute of ExitBlock from 0 should be empty") {
    block.junctionsForTrack(0)(HiddenTrack) should matchPattern {
      case (Some(0), None) =>
    }
  }

  property("trackGroupFor of ExitBlockTest for ExitBlock should return HiddenTrack") {
    val x = block.trackGroupFor(0)(HiddenTrack)
    x should have size (1)
    x should contain(HiddenTrack)
  }

  property("trackGroupFor of ExitBlockTest for other track should return empty") {
    block.trackGroupFor(0)(mock[Track]) shouldBe empty
  }

}