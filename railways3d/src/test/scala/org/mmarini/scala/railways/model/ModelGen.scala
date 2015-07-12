package org.mmarini.scala.railways.model

import org.scalacheck.Gen
import com.jme3.math.Vector2f

/**
 * 
 * @author us00852
 */
object ModelGen {
  val MaxCoordinate = 1e6f; // 1000 Km
  val MaxTimePerFrame = 2f;
  val MaxLength = MaxCoordinate;

  def chooseAngle: Gen[Float] = for (a <- Gen.choose(-Pif, Pif)) yield a

  def chooseCoord(special: Float*): Gen[Float] = {
    val newS = special ++ Array(0f, -0f, 1f, -1f, Float.MinPositiveValue, -Float.MinPositiveValue)
    Gen.chooseNum(-MaxCoordinate, MaxCoordinate, newS: _*)
  }

  def chooseLocation2f: Gen[Vector2f] = for {
    x <- chooseCoord()
    y <- chooseCoord()
  } yield new Vector2f(x, y)

  def chooseTimePerFrame: Gen[Float] = Gen.chooseNum(0f, MaxTimePerFrame, 0f, Float.MinPositiveValue)

  def chooseTime: Gen[Float] = Gen.posNum[Float]

  def chooseLength: Gen[Float] = Gen.chooseNum(0f, MaxLength, 0f, Float.MinPositiveValue);
}
