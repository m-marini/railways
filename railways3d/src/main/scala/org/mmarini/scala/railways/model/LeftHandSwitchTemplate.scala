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
  private val center2 = new Vector2f(CurveRadius - TrackGap, SegmentLength)
  private val forwardDivLeft = LeftCurveTrack(center1, CurveRadius, RightAngle, CurveLength / 2)
  private val forwardDivRight = RightCurveTrack(center2, CurveRadius, -RightAngle - CurveAngle / 2, CurveLength / 2)
  private val backwardDivRight = forwardDivLeft.backward
  private val backwardDivLeft = forwardDivRight.backward

  private val forwardDivTracks = IndexedSeq(forwardDivLeft, forwardDivRight)
  private val backwardDivTracks = IndexedSeq(backwardDivLeft, backwardDivRight)

  private val group = IndexedSeq(
    Set(Set[Track](forwardDirect, backwardDirect)),
    Set(Set[Track](forwardDivLeft, forwardDivRight, backwardDivLeft, backwardDivRight)))

  private val routes = IndexedSeq(
    IndexedSeq(
      Some(1, IndexedSeq[Track](forwardDirect)),
      Some(0, IndexedSeq[Track](backwardDirect)),
      None),
    IndexedSeq(
      Some(2, forwardDivTracks),
      None,
      Some(0, backwardDivTracks)))

  /** Returns track group for the only configuration */
  override def trackGroups(config: Int): Set[Set[Track]] = group(config)

  /** Returns junction routes */
  override def junctionRoute(config: Int)(junction: Int): Option[(Int, IndexedSeq[Track])] = routes(config)(junction)
}
