/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
case object Segment extends BlockTemplate {
  /** Returns 2 junction count */
  override val junctionCount = 2
}
