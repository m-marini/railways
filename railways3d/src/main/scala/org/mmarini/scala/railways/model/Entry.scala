/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** The incoming trains come in this BlockTemplate */
case object Entry extends BlockTemplate {
  /** Returns 1 */
  override val junctionCount = 1;
}
