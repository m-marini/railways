/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** The exiting trains go out this BlockTemplate */
case object Exit extends BlockTemplate {

  /** Returns track group for the only configuration */
  override def trackGroups(config: Int): Set[Set[Track]] = Set.empty

  /** Returns junction routes */
  def junctionRoute(config: Int)(junction: Int): Option[(Int, IndexedSeq[Track])] = None
}
