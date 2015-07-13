/**
 *
 */
package org.mmarini.scala.railways.model

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks
import com.jme3.math.Vector2f
import org.scalacheck.Gen
import ModelGen._
import org.mmarini.scala.railways.model.tracks.Track

/** Test */
class MovingTrainTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  property("transitTrack of MovingTrainTest should be all train tracks") {
    forAll(
      (Gen.chooseNum(2, 4), "size"),
      (chooseLength, "location")) {
        (size: Int, location: Float) =>
          {
            val trackPath = Seq(mock[Track])
            val route = mock[TrainRoute]
            when(route.pathTracks(location - size * CoachLength, location)) thenReturn trackPath

            val train = MovingTrain("", size, route, location, 0f)
            val tt = train.transitTracks
            verify(route).pathTracks(location - size * CoachLength, location)
            tt shouldBe (trackPath)
          }
      }
  }
}