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
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model.tracks.RightCurveTrack
import scala.math._
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model.tracks.LeftCurveTrack
import org.mmarini.scala.railways.model.tracks.LeftCurveTrack
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model.tracks.RightCurveTrack

/** */
case class LineSwitchBlock(
  id: String,
  trans: Transform2d,
  trackGroups: IndexedSeq[Set[Set[Track]]],
  routes: IndexedSeq[Set[(Int, Int, IndexedSeq[Track])]])
    extends Block
    with TrackBlock
    with SwitchBlock

/** Factory of [[LineSwitchBlock]] */
object LineSwitchBlock {
  private val Radius = 400f

  private val OuterRadius = Radius + TrackGaudge / 2
  private val InnerRadius = Radius - TrackGaudge / 2
  private val Cos = InnerRadius / OuterRadius
  private val HeartDy = sqrt(OuterRadius * OuterRadius - InnerRadius * InnerRadius).toFloat
  private val Sin = HeartDy / OuterRadius
  private val Tan = HeartDy / InnerRadius
  private val CurveAngle = acos(Cos).toFloat
  private val CurveLength = Radius * CurveAngle
  private val CurveDx = Radius * (1 - Cos)
  private val CurveDy = Radius * Sin
  private val JoinDx = TrackGap - CurveDx * 2
  private val JoinDy = JoinDx / Tan
  val Length = CurveDy * 4 + JoinDy * 2

  /**
   *  Creates a [[LineSwitchBlock]]
   *
   *         H     I   --h0.
   *         |\    |
   *         | G   |
   *         |  \  |
   *         |   F |
   *         |    \|
   * e0--    |     E
   *         |    /|
   *         |   D |
   *         |  /  |
   *         | C   |
   *         |/    |
   *         A     B   --a0
   */
  def apply(id: String, x: Float, y: Float, orientation: Float): SwitchBlock = {
    // southern points
    val a = new Vector2f(-TrackGap / 2, 0f)
    val a0 = a.add(new Vector2f(Radius, 0))
    val b = new Vector2f(TrackGap / 2, 0f)
    val c = a.add(new Vector2f(CurveDx, CurveDy))
    val d = c.add(new Vector2f(JoinDx, JoinDy))
    val e = new Vector2f(TrackGap / 2, CurveDy * 2 + JoinDy)
    val e0 = e.add(new Vector2f(-Radius, 0))
    val f = e.add(new Vector2f(-CurveDx, CurveDy))
    val g = f.add(new Vector2f(-JoinDx, JoinDy))
    val h = new Vector2f(-TrackGap / 2, Length)
    val h0 = h.add(new Vector2f(Radius, 0))
    val i = new Vector2f(TrackGap / 2, Length)

    val trans = Transform2d(x, y, orientation)

    val ah = SegmentTrack(a, h)(trans)
    val ac = RightCurveTrack(a0, Radius, -RightAngle, CurveLength)(trans)
    val be = SegmentTrack(b, e)(trans)
    val bi = SegmentTrack(b, i)(trans)
    val cd = SegmentTrack(c, d)(trans)
    val de = LeftCurveTrack(e0, Radius, RightAngle + CurveAngle, CurveLength)(trans)
    val ef = LeftCurveTrack(e0, Radius, RightAngle, CurveLength)(trans)
    val ei = SegmentTrack(e, i)(trans)
    val fg = SegmentTrack(f, g)(trans)
    val gh = RightCurveTrack(h0, Radius, -RightAngle - CurveAngle, CurveLength)(trans)

    val Some(ha) = ah.backward
    val Some(ca) = ac.backward
    val Some(eb) = be.backward
    val Some(ib) = bi.backward
    val Some(dc) = cd.backward
    val Some(ed) = de.backward
    val Some(fe) = ef.backward
    val Some(ie) = ei.backward
    val Some(gf) = fg.backward
    val Some(hg) = gh.backward

    val ahRoute = IndexedSeq(ah)
    val aeiRoute = IndexedSeq(ac, cd, de, ei)
    val behRoute = IndexedSeq(be, ef, fg, gh)
    val biRoute = IndexedSeq(bi)

    val haRoute = IndexedSeq(ha)
    val ieaRoute = IndexedSeq(ie, ed, dc, ca)
    val hebRoute = IndexedSeq(hg, gf, fe, eb)
    val ibRoute = IndexedSeq(ib)

    val aehRoute = IndexedSeq(ac, cd, de, ef, fg, gh)
    val heaRoute = IndexedSeq(hg, gf, fe, ed, dc, ca)

    val group0 = Set((ahRoute ++ haRoute).toSet, (biRoute ++ ibRoute).toSet)
    val group1 = Set((aeiRoute ++ ieaRoute).toSet)
    val group2 = Set((behRoute ++ hebRoute).toSet)
    val group3 = Set((aehRoute ++ heaRoute).toSet)

    val groups = IndexedSeq(group0, group1, group2, group3)

    val route0 = Set((0, 2, ahRoute), (2, 0, haRoute), (1, 3, biRoute), (3, 1, ibRoute))
    val route1 = Set((0, 3, aeiRoute), (3, 0, ieaRoute))
    val route2 = Set((1, 2, behRoute), (2, 1, hebRoute))
    val route3 = Set((0, 2, aehRoute), (2, 0, heaRoute))

    val routes = IndexedSeq(route0, route1, route2, route3)

    new LineSwitchBlock(id, trans, groups, routes)
  }
}
