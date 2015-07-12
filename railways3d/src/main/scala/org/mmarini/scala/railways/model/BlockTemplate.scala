/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/**
 * Produces trajectory depending on configuration of block
 * For each configuration there are some mutex track groups
 * For each configuration there are several connected junction pairs
 * For each configuration there are trajectory connecting junction pairs
 */
trait BlockTemplate {

  /** Returns track group for the only configuration */
  def trackGroups(config: Int): Set[Track]

  /** Returns junction routes */
  def junctionRoute(config: Int)(junction: Int): Option[(Int, IndexedSeq[Track])]
}
