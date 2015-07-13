/**
 *
 */
package org.mmarini.scala.railways.model

import com.jme3.math.Vector2f

/** */
case class RightHandSwitchBlock(
  id: String,
  trans: Transform2d,
  trackGroups: IndexedSeq[Set[Set[Track]]],
  tracksForJunction: IndexedSeq[IndexedSeq[(Option[Int], IndexedSeq[Track])]])
    extends Block
    with TrackBlock
    with SwitchBlock

/** Factory of [[LeftHandSwitchBlock]] */
object RightHandSwitchBlock {

  /** Creates a [[LeftHandSwitchBlock]] */
  def apply(id: String, x: Float, y: Float, orientation: Float): RightHandSwitchBlock = {
    val trans = Transform2d(x, y, orientation)
    val forwardDirect = SegmentTrack(trans(Vector2f.ZERO), trans(new Vector2f(0f, SegmentLength)))
    val backwardDirect = forwardDirect.backward
    val center1 = trans(new Vector2f(CurveRadius, 0f))
    val center2 = trans(new Vector2f(TrackGap - CurveRadius, SegmentLength))
    val forwardDivRight = RightCurveTrack(center1, CurveRadius, -RightAngle + orientation, CurveLength / 2)
    val forwardDivLeft = LeftCurveTrack(center2, CurveRadius, RightAngle + CurveAngle / 2 + orientation, CurveLength / 2)
    val backwardDivRight = forwardDivLeft.backward
    val backwardDivLeft = forwardDivRight.backward

    val forwardDivTracks = IndexedSeq[Track](forwardDivRight, forwardDivLeft)
    val backwardDivTracks = IndexedSeq[Track](backwardDivRight, backwardDivLeft)

    val group = IndexedSeq(
      Set(Set[Track](forwardDirect, backwardDirect)),
      Set(Set[Track](forwardDivLeft, forwardDivRight, backwardDivLeft, backwardDivRight)))

    val routes = IndexedSeq(
      IndexedSeq(
        (Some(1), IndexedSeq[Track](forwardDirect)),
        (Some(0), IndexedSeq[Track](backwardDirect)),
        (None, IndexedSeq())),
      IndexedSeq(
        (Some(2), forwardDivTracks),
        (None, IndexedSeq()),
        (Some(0), backwardDivTracks)))
    RightHandSwitchBlock(id, trans, group, routes)
  }
}
