/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model._
import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model.Transform2d

/** The arriving trains stop at this [[Block]] to wait for passengers boarding */
case class PlatformBlock(
  id: String,
  trans: Transform2d,
  trackGroups: IndexedSeq[Set[Set[Track]]],
  routes: IndexedSeq[Set[(Option[Int], Option[Int], IndexedSeq[Track])]])
    extends Block with TrackBlock

/** Factory of [[SegmentBlock]] */
object PlatformBlock {

  /** Creates a [[SegmentBlock]] */
  def apply(id: String, x: Float, y: Float, orientation: Float): PlatformBlock = {
    val trans = Transform2d(x, y, orientation)
    val forward = SegmentTrack(Vector2f.ZERO, new Vector2f(0f, 11 * SegmentLength))(trans)
    val backward = forward.backward
    val group = IndexedSeq(Set(Set[Track](forward, backward)))
    val routes = IndexedSeq(
      Set(
        (Option(0), Option(1), IndexedSeq[Track](forward)),
        (Option(1), Option(0), IndexedSeq[Track](backward))))
    PlatformBlock(id, trans, group, routes)
  }
}   
