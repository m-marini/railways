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
//            val route = mock[TrainRoute]
//            val train = WaitForPassengerTrain("train", 2, route, 0f, timeout)
//            val status = mock[GameStatus]
//            when(status.putTrain(null)).thenReturn(status)
//
//            status.putTrain(train) shouldBe (status)
//            
//            verify(status).putTrain(train)
//            
//            val next = train.tick(dt, status);
//            
//
//            next shouldBe a[StoppedTrain]
          }
        }
    }
  }
  property("WaitForPassengerTrain should change timeout") {

    forAll((ModelGen.chooseTime, "timeout"), (ModelGen.chooseTimePerFrame, "dt")) {
      (timeout: Float, dt: Float) =>
        {
          whenever(dt < timeout) {
//            val train = WaitForPassengerTrain("train", 2, TrainRoute(IndexedSeq.empty), 0f, timeout)
//            val status = mock[GameStatus]
//            val next = train.tick(dt, status);
//
//            next shouldBe a[WaitForPassengerTrain]
//            next should have('timeout(0f))
          }
        }
    }
  }
}