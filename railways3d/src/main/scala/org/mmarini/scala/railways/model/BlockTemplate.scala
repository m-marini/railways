/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** Produces trajectory depending on configuration of block */
trait BlockTemplate {
  /** Returns the number of junctions */
  def junctonCount: Int

  /** Returns the number of configurations */
  def confCount: Int

  /** Returns the trajectory and true if reverse direction for a given junction and configuration */
  def trajectory(junction: Int, conf: Int): Option[(Trajectory)]

}

trait TrajectorySupport {

  protected def trajectories: Seq[Seq[Option[Trajectory]]]

  /** Returns the number of configurations */
  def confCount: Int = trajectories.size

  /** Returns the trajectory and true if reverse direction for a given junction and configuration */
  def trajectory(conf: Int, junction: Int): Option[Trajectory] =
    if (trajectories.isDefinedAt(conf) && trajectories(conf).isDefinedAt(junction)) {
      trajectories(conf)(junction)
    } else {
      None
    }
}

/** The incoming trains come in this BlockTemplate */
case object Entry extends BlockTemplate with TrajectorySupport {
  /** Returns the number of junctions */
  val junctonCount = 1

  protected val trajectories = Seq.empty[Seq[Option[Trajectory]]]
}

/** The exiting trains go out this BlockTemplate */
case object Exit extends BlockTemplate with TrajectorySupport {
  /** Returns the number of junctions */
  val junctonCount = 1

  protected val trajectories = Seq.empty[Seq[Option[Trajectory]]]
}

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
case object Platform extends BlockTemplate with TrajectorySupport {
  /** Returns the number of configurations */
  val junctonCount = 2

  /** Creates the trajectories configuration */
  private def createConf = {
    val trackList = IndexedSeq(LinearTrack(new Vector2f, new Vector2f(0f, SegmentLength * 11)))

    IndexedSeq(IndexedSeq(
      Option(Trajectory(trackList, false)),
      Option(Trajectory(trackList, true))))
  }

  protected val trajectories = createConf
}

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
case object TrackTemplate extends BlockTemplate with TrajectorySupport {
  /** Returns the number of configurations */
  val junctonCount = 2

  /** Creates the trajectories configuration */
  private def createConf = {
    val trackList = IndexedSeq(LinearTrack(new Vector2f, new Vector2f(0f, SegmentLength * 11)))

    IndexedSeq(IndexedSeq(
      Option(Trajectory(trackList, false)),
      Option(Trajectory(trackList, true))))
  }

  protected val trajectories = createConf
}

/** */
case object LeftHandSwitch extends BlockTemplate with TrajectorySupport {
  /** Returns the number of junctions */
  val junctonCount = 3

//  /** Creates the configurations of switch */
//  private def createConf = {
//    val straightTrack = IndexedSeq(LinearTrack(new Vector2f, new Vector2f(0f, SegmentLength)))
//
//    val divTrack = IndexedSeq(RightCurveTrack(new Vector2f(-CurveRadius, 0f), CurveRadius, RightAngle, RightAngle - CurveAngle))
//
//    val straightConf = IndexedSeq(
//      Option(Trajectory(straightTrack, false)),
//      Option(Trajectory(straightTrack, true)),
//      None)
//
//    val divConf = IndexedSeq(
//      Option(Trajectory(divTrack, false)),
//      None,
//      Option(Trajectory(divTrack, true)))
//
//    IndexedSeq(straightConf, divConf)
//  }

  protected val trajectories = Seq.empty[Seq[Option[Trajectory]]]
//  protected val trajectories = createConf
}

/** */
case object RightHandSwitch extends BlockTemplate with TrajectorySupport {
  /** Returns the number of junctions */
  val junctonCount = 3

//  /** Creates the configurations of switch */
//  private def createConf = {
//    val straightTrack = IndexedSeq(LinearTrack(new Vector2f, new Vector2f(0f, SegmentLength)))
//
//    val divTrack = IndexedSeq(RightCurveTrack(new Vector2f(CurveRadius, 0f), CurveRadius, -RightAngle, -RightAngle + CurveAngle))
//
//    val straightConf = IndexedSeq(
//      Option(Trajectory(straightTrack, false)),
//      Option(Trajectory(straightTrack, true)),
//      None)
//    val divConf = IndexedSeq(
//      Option(Trajectory(divTrack, false)),
//      None,
//      Option(Trajectory(divTrack, true)))
//
//    IndexedSeq(straightConf, divConf)
//  }

//  protected val trajectories = createConf
  protected val trajectories = Seq.empty[Seq[Option[Trajectory]]]
}
