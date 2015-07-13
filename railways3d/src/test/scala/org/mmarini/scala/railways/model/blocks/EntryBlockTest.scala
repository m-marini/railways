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
import org.mmarini.scala.railways.model.tracks.EntryTrack

/** Test */
class EntryBlockTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val block = EntryBlock("", 0, 0, 0)

  property("junctionRoute of Segment from 0 should be empty") {
    val jr0 = block.tracksForJunction(0)(0)
    jr0 match {
      case (None, list) =>
        list should have size (1)
        list should contain(EntryTrack)
      case x => fail(s"$x not match")
    }
  }
}