/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model._
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model.tracks.RightCurveTrack
import org.mmarini.scala.railways.model.tracks.LeftCurveTrack
import com.jme3.math.Vector2f
import org.mmarini.scala.railways.model.tracks.Track

/** */
case class RightHandSwitchBlock(
  id: String,
  trans: Transform2d,
  trackGroups: IndexedSeq[Set[Set[Track]]],
  routes: IndexedSeq[Set[(Option[Int], Option[Int], IndexedSeq[Track])]])
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
      Set(
        (Option(0), Option(1), IndexedSeq[Track](forwardDirect)),
        (Option(1), Option(0), IndexedSeq[Track](backwardDirect)),
        (Option(2), None, IndexedSeq())),
      Set(
        (Option(0), Option(2), forwardDivTracks),
        (Option(1), None, IndexedSeq()),
        (Option(2), Option(0), backwardDivTracks)))
    RightHandSwitchBlock(id, trans, group, routes)
  }
}
