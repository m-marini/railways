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

/** Test */
class SegmentStatusTest extends PropSpec with Matchers with PropertyChecks with MockitoSugar {

  property("trackGroupFor of SegmentStatus should return all tracks (forward and backward)") {
  }
}