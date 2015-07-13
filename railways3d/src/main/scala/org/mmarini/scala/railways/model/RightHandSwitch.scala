/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** */
case object RightHandSwitch extends BlockTemplate {

  /** Returns track group for the only configuration */
  override def trackGroups(config: Int): Set[Set[Track]] = ??? // TODO

  /** Returns junction routes */
  override def junctionRoute(config: Int)(junction: Int): Option[(Int, IndexedSeq[Track])] = ??? //TODO
}
