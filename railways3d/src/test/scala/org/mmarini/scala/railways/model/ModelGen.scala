package org.mmarini.scala.railways.model

import org.scalacheck.Gen
import com.jme3.math.Vector2f

/**
 * @author us00852
 */
object ModelGen {
  val MaxCoordinate = 1e6f; // 1000 Km
  val MaxTimePerFrame = 2f;

  def chooseAngle = for (a <- Gen.choose(-Pif, Pif)) yield a

  def chooseCoord(special: Float*) = {
    val newS = special ++ Array(0f, -0f, 1f, -1f, Float.MinPositiveValue, -Float.MinPositiveValue)
    Gen.chooseNum(-MaxCoordinate, MaxCoordinate, newS: _*)
  }

  def chooseLocation2f = for {
    x <- chooseCoord()
    y <- chooseCoord()
  } yield new Vector2f(x, y)

  def chooseTimePerFrame = Gen.chooseNum(0f, MaxTimePerFrame, 0f, Float.MinPositiveValue);

  def chooseTime = Gen.posNum[Float]

}
