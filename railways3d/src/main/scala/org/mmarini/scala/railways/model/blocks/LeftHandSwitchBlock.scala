/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model._
import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.tracks.LeftCurveTrack
import org.mmarini.scala.railways.model.tracks.RightCurveTrack
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model.Transform2d

/** */
case class LeftHandSwitchBlock(
  id: String,
  trans: Transform2d,
  trackGroups: IndexedSeq[Set[Set[Track]]],
  routes: IndexedSeq[Set[(Int, Int, IndexedSeq[Track])]])
    extends Block
    with TrackBlock
    with SwitchBlock

/** Factory of [[LeftHandSwitchBlock]] */
object LeftHandSwitchBlock {

  /** Creates a [[LeftHandSwitchBlock]] */
  def apply(id: String, x: Float, y: Float, orientation: Float): LeftHandSwitchBlock = {
    val trans = Transform2d(x, y, orientation)
    val forwardDirect = SegmentTrack(Vector2f.ZERO, new Vector2f(0f, SegmentLength))(trans)
    val Some(backwardDirect) = forwardDirect.backward
    val center1 = new Vector2f(-CurveRadius, 0f)
    val center2 = new Vector2f(CurveRadius - TrackGap, SegmentLength)
    val forwardDivLeft = LeftCurveTrack(center1, CurveRadius, RightAngle, CurveLength / 2)(trans)
    val forwardDivRight = RightCurveTrack(center2, CurveRadius, -RightAngle - CurveAngle / 2, CurveLength / 2)(trans)
    val Some(backwardDivRight) = forwardDivLeft.backward
    val Some(backwardDivLeft) = forwardDivRight.backward

    val forwardDivTracks = IndexedSeq[Track](forwardDivLeft, forwardDivRight)
    val backwardDivTracks = IndexedSeq[Track](backwardDivLeft, backwardDivRight)

    val group = IndexedSeq(
      Set(Set[Track](forwardDirect, backwardDirect)),
      Set(Set[Track](forwardDivLeft, forwardDivRight, backwardDivLeft, backwardDivRight)))

    val routes = IndexedSeq(
      Set(
        (0, 1, IndexedSeq[Track](forwardDirect)),
        (1, 0, IndexedSeq[Track](backwardDirect))),
      Set(
        (0, 2, forwardDivTracks),
        (2, 0, backwardDivTracks)))
    LeftHandSwitchBlock(id, trans, group, routes)
  }
}
