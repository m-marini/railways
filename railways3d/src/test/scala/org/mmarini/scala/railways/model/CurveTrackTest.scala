/**
 *
 */
package org.mmarini.scala.railways.model

import org.scalatest.Matchers
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks
import com.jme3.math.Vector2f
import org.scalacheck.Gen

class CurveTrackTest extends PropSpec with Matchers with PropertyChecks {

  val angleGen = for (a <- Gen.choose(-Pif, Pif)) yield a
  val distanceGen = for (d <- Gen.chooseNum(-1e6f, 1e6f, 0f, CurveLength)) yield d
  val coordGen = for {
    x <- Gen.chooseNum(-1e6f, 1e6f, 0f)
    y <- Gen.chooseNum(-1e6f, 1e6f, 0f)
  } yield new Vector2f(x, y)

  property("The location a left curve should be radius distant from center") {
    forAll((coordGen, "center"), (distanceGen, "distance"), (angleGen, "begin")) {
      (center: Vector2f, distance: Float, begin: Float) =>
        whenever(distance >= 0 && distance <= CurveLength) {
          val curve = LeftCurveTrack(center, CurveRadius, begin, CurveLength)
          val p = curve.locationAt(distance)
          p should not be empty
          p.foreach(p => p.distance(center) should be(CurveRadius +- 0.1f))
        }
    }
  }
}
