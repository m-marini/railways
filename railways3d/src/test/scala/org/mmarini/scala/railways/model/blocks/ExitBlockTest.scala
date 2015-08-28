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
import org.mmarini.scala.railways.model.tracks.Track

/** Test */
class ExitBlockTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val block = ExitBlock("", 0, 0, 0)

  property("tracksForJunction of ExitBlock from 0 should the exit track") {
    val x = block.tracksForJunction
    x(0)(0) should have size (1)
    x(0)(0)(0) shouldBe (block.track)
  }

  property("tracksForJunction of ExitBlock from 1 should empty") {
    val x = block.tracksForJunction
    x(0)(1) shouldBe empty
  }

  property("junctionsForTrack for exit track should be Some(0, 1)") {
    block.junctionsForTrack(0)(block.track) shouldBe (Some(0, 1))
  }

  property("junctionsForTrack for other track should be empty") {
    block.junctionsForTrack(0)(mock[Track]) shouldBe empty
  }

  property("trackGroupFor of ExitBlockTest for ExitBlock should return HiddenTrack") {
    val x = block.trackGroupFor(0)(block.track)
    x should have size (1)
    x should contain(block.track)
  }

  property("trackGroupFor of ExitBlockTest for other track should return empty") {
    block.trackGroupFor(0)(mock[Track]) shouldBe empty
  }
}
