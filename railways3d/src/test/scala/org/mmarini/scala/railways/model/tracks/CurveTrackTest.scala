/**
 *
 */
package org.mmarini.scala.railways.model.tracks

import org.scalatest.Matchers
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks
import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model.ModelGen
import org.mmarini.scala.railways.model._

class CurveTrackTest extends PropSpec with Matchers with PropertyChecks {

  property("The location a left curve should be radius distant from center") {
    forAll(
      (ModelGen.chooseLocation2f, "center"),
      (ModelGen.chooseCoord(CurveLength), "distance"),
      (ModelGen.chooseAngle, "begin")) {
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
