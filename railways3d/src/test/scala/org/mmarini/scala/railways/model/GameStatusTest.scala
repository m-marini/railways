/**
 *
 */
package org.mmarini.scala.railways.model

import org.scalatest.PropSpec
import org.scalatest.Matchers
import org.scalatest.prop.PropertyChecks
import scala.math.sqrt
import scala.math.atan2
import scala.math.Pi
import org.scalacheck.Gen
import scala.util.Random
import org.mmarini.scala.railways.model.blocks.BlockStatus

class GameStatusTest extends PropSpec with Matchers with PropertyChecks {

  property("The time of next game status after elapsed time should be added by elapsed time") {
    val s = GameStatus(
      parameters = GameParameters(),
      stationStatus = StationStatus(Downville, Set[BlockStatus]()),
      random = new Random(),
      trains = Set.empty,
      performance = GamePerformance())

    forAll {
      (elapsed: Float) =>
        val ns = s.tick(elapsed)
        ns.performance.elapsedTime should be(elapsed)
    }
  }
}
