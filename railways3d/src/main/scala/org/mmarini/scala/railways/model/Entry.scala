/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** The incoming trains come in this BlockTemplate */
case object Entry extends BlockTemplate {

  private val group = Set[Track](EntryTrack)
  
  /** Returns track group for the only configuration */
  override def trackGroups(config: Int): Set[Track] = group

  /** Returns junction routes */
  def junctionRoute(config: Int)(junction: Int): Option[(Int, IndexedSeq[Track])] = None
}
