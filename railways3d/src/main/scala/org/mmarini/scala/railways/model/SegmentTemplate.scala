/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
case object SegmentTemplate extends BlockTemplate {

  private val forward = SegmentTrack(Vector2f.ZERO, new Vector2f(0f, 11 * SegmentLength))
  private val backward = forward.backward
  private val group = Set(Set[Track](forward, backward))
  private val routes = IndexedSeq(
    IndexedSeq((1, IndexedSeq[Track](forward)), (0, IndexedSeq[Track](backward))))

  /** Returns track group for the only configuration */
  def trackGroups(config: Int): Set[Set[Track]] = group

  /** Returns junction routes */
  def junctionRoute(config: Int)(junction: Int): Option[(Int, IndexedSeq[Track])] = Option(routes(config)(junction))
}   
