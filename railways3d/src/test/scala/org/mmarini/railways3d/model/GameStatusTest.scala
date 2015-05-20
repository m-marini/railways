package org.mmarini.railways3d.model

import org.scalatest.PropSpec
import org.scalatest.Matchers
import org.scalatest.prop.PropertyChecks
import scala.math.sqrt
import scala.math.atan2
import scala.math.Pi
import org.scalacheck.Gen

class GameStatusTest extends PropSpec with Matchers with PropertyChecks {

  property("The time of next game status after elapsed time should be added by elapsed time") {
    val s = GameStatus(0f, Map())

    forAll {
      (elapsed: Float) =>
        val ns = s.tick(elapsed)
        ns.time should be(elapsed)
    }
  }
}