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
class TrainRouteTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  property("TrainRoute length should be the sum of length of tracks") {

    forAll((Gen.listOf(chooseLength), "lengths")) {
      (lengths: List[Float]) =>
        whenever(!lengths.isEmpty) {
          val expected = lengths.sum
          val route = createTrainRoute(lengths)

          route.length shouldBe (expected)
        }
    }
  }

  property("TrainRoute location should exists for distance within length") {
    forAll(
      (Gen.listOfN(3, chooseLength), "lengths"),
      (chooseCoord(), "location")) {
        (lengths: List[Float], location: Float) =>
          {
            val length = if (lengths.isEmpty) 0f else lengths.sum
            whenever(!lengths.isEmpty && location >= 0 && location <= length) {

              val route = createTrainRoute(lengths)

              route.locationAt(location) should not be empty

            }
          }
      }
  }

  property("TrainRoute location should not exists for distance out of length") {
    forAll(
      (Gen.listOfN(3, chooseLength), "lengths"),
      (chooseCoord(), "location")) {
        (lengths: List[Float], location: Float) =>
          {
            val length = if (lengths.isEmpty) 0f else lengths.sum
            whenever(!lengths.isEmpty && (location < 0 || location > length)) {

              val route = createTrainRoute(lengths)

              route.locationAt(location) shouldBe empty

            }
          }
      }
  }

  property("pathTracks of TrainRoute for 1st track should extract 1st track") {
    val testGen = for {
      length <- chooseLength if length > 0f
      from <- Gen.chooseNum(0f, length)
      d <- Gen.chooseNum(0f, length) if (from + d < length)
    } yield (length, from, from + d)

    forAll((testGen, "testCase")) {
      case (length: Float, from: Float, to: Float) =>
        val route = createTrainRoute(List(length, length, length))
        val pt = route.pathTracks(from, to)
        pt.size shouldBe (1)
        pt(0) shouldBe route.tracks(0)
    }
  }

  property("pathTracks of TrainRoute for 2nd track should extract 2nd track") {
    val testGen = for {
      length <- chooseLength if length > 0f
      p <- Gen.chooseNum(0f, length) if (p + length > length)
      d <- Gen.chooseNum(0f, length) if (p + length + d < 2 * length)
    } yield (length, p + length, p + length + d)

    forAll((testGen, "testCase")) {
      case (length: Float, from: Float, to: Float) =>
        val route = createTrainRoute(List(length, length, length))
        val pt = route.pathTracks(from, to)
        pt.size shouldBe (1)
        pt(0) shouldBe route.tracks(1)
    }
  }

  property("pathTracks of TrainRoute for 3rd track should extract 3rd track") {
    val testGen = for {
      length <- chooseLength if length > 0f
      p <- Gen.chooseNum(0f, length) if (p + 2 * length > 2 * length)
      d <- chooseLength
    } yield (length, p + 2 * length, p + 2 * length + d)

    forAll((testGen, "testCase")) {
      case (length: Float, from: Float, to: Float) =>
        val route = createTrainRoute(List(length, length, length))
        val pt = route.pathTracks(from, to)
        pt.size shouldBe (1)
        pt(0) shouldBe route.tracks(2)
    }
  }

  property("pathTracks of TrainRoute for 1st 2 tracks should extract 1st 2 tracks") {
    val testGen = for {
      length <- chooseLength if length > 0f
      from <- Gen.chooseNum(0f, length)
      d <- Gen.chooseNum(0f, length) if (from + length + d < 2 * length)
    } yield (length, from, from + length + d)

    forAll((testGen, "testCase")) {
      case (length: Float, from: Float, to: Float) =>
        val route = createTrainRoute(List(length, length, length))
        val pt = route.pathTracks(from, to)
        pt.size shouldBe (2)
        pt(0) shouldBe route.tracks(0)
        pt(1) shouldBe route.tracks(1)
    }
  }

  property("pathTracks of TrainRoute for 2nd 2 tracks should extract 2nd 2 tracks") {
    val testGen = for {
      length <- chooseLength if length > 0f
      p <- Gen.chooseNum(0f, length) if (p + length > length)
      d <- chooseLength if (d >= 2)
    } yield (length, p + length, p + 2 * length + d)

    forAll((testGen, "testCase")) {
      case (length: Float, from: Float, to: Float) =>
        val route = createTrainRoute(List(length, length, length))
        val pt = route.pathTracks(from, to)
        pt.size shouldBe (2)
        pt(0) shouldBe route.tracks(1)
        pt(1) shouldBe route.tracks(2)
    }
  }

  property("pathTracks of TrainRoute for all tracks should extract all tracks") {
    val testGen = for {
      length <- chooseLength if length > 0f
      p <- Gen.chooseNum(-MaxLength, length) if (p > 0)
      d <- chooseLength if (p + 3 * length + d >= 3 * length)
    } yield (length, p, p + 3 * length + d)

    forAll((testGen, "testCase")) {
      case (length: Float, from: Float, to: Float) =>
        val route = createTrainRoute(List(length, length, length))
        val pt = route.pathTracks(from, to)
        pt.size shouldBe (3)
        pt(0) shouldBe route.tracks(0)
        pt(1) shouldBe route.tracks(1)
        pt(2) shouldBe route.tracks(2)
    }
  }

  private def createTrainRoute(lengths: List[Float]) =
    TrainRoute(lengths.map(len => {
      val track = mock[Track]
      when(track.length).thenReturn(len)
      when(track.locationAt(any[Float])).thenReturn(Some(Vector2f.ZERO))
      track
    }).toIndexedSeq)
}