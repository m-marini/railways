/**
 *
 */
package org.mmarini.scala.railways.model

import org.mockito.Mockito._
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks
import com.jme3.math.Vector2f
import org.scalacheck.Gen

/** Test */
class WaitForPassengerTrainTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  property("WaitForPassengerTrain should change status after time expiration") {

    forAll((ModelGen.chooseTimePerFrame, "timeout"), (ModelGen.chooseTimePerFrame, "dt")) {
      (timeout: Float, dt: Float) =>
        {
          whenever(dt >= timeout) {
          }
        }
    }
  }
  property("WaitForPassengerTrain should change timeout") {

    forAll((ModelGen.chooseTime, "timeout"), (ModelGen.chooseTimePerFrame, "dt")) {
      (timeout: Float, dt: Float) =>
        {
          whenever(dt < timeout) {
          }
        }
    }
  }
}
