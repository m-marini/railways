/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
case class PlatformBlock(
  id: String,
  trans: Transform2d,
  trackGroups: IndexedSeq[Set[Set[Track]]],
  tracksForJunction: IndexedSeq[IndexedSeq[(Option[Int], IndexedSeq[Track])]]) extends Block with TrackBlock

/** Factory of [[SegmentBlock]] */
object PlatformBlock {

  /** Creates a [[SegmentBlock]] */
  def apply(id: String, x: Float, y: Float, orientation: Float): PlatformBlock = {
    val trans = Transform2d(x, y, orientation)
    val forward = SegmentTrack(trans(Vector2f.ZERO), trans(new Vector2f(0f, 11 * SegmentLength)))
    val backward = forward.backward
    val group = IndexedSeq(Set(Set[Track](forward, backward)))
    val routes = IndexedSeq(
      IndexedSeq((Some(1), IndexedSeq[Track](forward)), (Some(0), IndexedSeq[Track](backward))))
    PlatformBlock(id, trans, group, routes)
  }
}   
