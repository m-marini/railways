/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus]] of an exit [[Block]] */
case class ExitStatus(
    block: ExitBlock,
    trainId: Option[String] = None,
    locked: Boolean = false) extends SingleBlockStatus {

  /** */
  override def changeFreedom = ExitStatus(block, trainId, !locked)

  /** Returns None */
  override def junctionFrom = (x) => if (x == 0) Some(1) else None

  /** Returns the transit train in a junction */
  override def transitTrain = _ => trainId

  /** Creates a new block status applying trainId to a junction. */
  override def apply(junction: Int, trainId: Option[String]) = junction match {
    case 0 if (trainId != this.trainId) => ExitStatus(block, trainId)
    case _ => this
  }

  /** Returns the status with no transit train */
  override def noTrainStatus: BlockStatus = if (trainId.isEmpty) this else ExitStatus(block, None, locked)

  /** Returns true if the junction is clear. */
  override def isClear = (x) => !locked && transitTrain(x).isEmpty
}
