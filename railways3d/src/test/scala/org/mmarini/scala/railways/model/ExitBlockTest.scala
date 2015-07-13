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
import org.scalacheck.Arbitrary
import scala.collection.immutable.Vector

/** Test */
class ExitTemplateTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {
  val block = ExitBlock("", 0, 0, 0)

  property("junctionRoute of ExitBlock from 0 should be empty") {
    val jr0 = block.tracksForJunction(0)(0)
    jr0 match {
      case (None, IndexedSeq()) =>
      case x => fail(s"$x not match")
    }
  }
}