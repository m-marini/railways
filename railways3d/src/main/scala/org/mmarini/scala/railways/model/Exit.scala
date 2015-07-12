/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** The exiting trains go out this BlockTemplate */
case object Exit extends BlockTemplate {
  /** Returns 1 */
  override val junctionCount = 1;
}
