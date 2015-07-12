/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** */
case object LeftHandSwitchTemplate extends BlockTemplate {

  private val forwardDirect = SegmentTrack(Vector2f.ZERO, new Vector2f(0f, SegmentLength))
  private val backwardDirect = forwardDirect.backward
  private val center1 = new Vector2f(-CurveRadius, 0f)
  private val center2 = new Vector2f(-TrackGap + CurveRadius, SegmentLength)
  private val forwardDevLeft = LeftCurveTrack(center1, CurveRadius, StraightAngle, CurveLength / 2)
  private val forwardDevRight = RightCurveTrack(center2, CurveRadius, -CurveAngle / 2, CurveLength / 2)
  private val backwardDevRight = forwardDevLeft.backward
  private val backwardDevLeft = forwardDevRight.backward

  private val forwardDevTracks = IndexedSeq(forwardDevLeft, forwardDevRight)
  private val backwardDevTracks = IndexedSeq(backwardDevRight, backwardDevLeft)

  private val group = IndexedSeq(
    Set[Track](forwardDirect, backwardDirect),
    Set[Track](forwardDevLeft, forwardDevRight, backwardDevLeft, backwardDevRight))

  private val routes = IndexedSeq(
    IndexedSeq(
      Some(1, IndexedSeq[Track](forwardDirect)),
      Some(0, IndexedSeq[Track](backwardDirect)),
      None),
    IndexedSeq(
      Some(2, forwardDevTracks),
      None,
      Some(0, backwardDevTracks)))

  /** Returns track group for the only configuration */
  override def trackGroups(config: Int): Set[Track] = group(config)

  /** Returns junction routes */
  override def junctionRoute(config: Int)(junction: Int): Option[(Int, IndexedSeq[Track])] = routes(config)(junction)
}
