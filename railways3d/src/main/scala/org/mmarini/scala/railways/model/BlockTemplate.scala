/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** Produces trajectory depending on configuration of block */
trait BlockTemplate {
  /** Return the number of junctions */
  def junctionCount: Int
}
