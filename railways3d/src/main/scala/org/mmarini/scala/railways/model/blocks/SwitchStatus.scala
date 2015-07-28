/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus of switch */
case class SwitchStatus(
    block: SwitchBlock,
    trainId: Option[String] = None,
    lockedJunctions: IndexedSeq[Boolean] = IndexedSeq(false, false, false),
    diverging: Boolean = false) extends IndexedBlockStatus with LockableStatus {

  private val junctions = IndexedSeq(
    IndexedSeq(
      Some(1),
      Some(0),
      None),
    IndexedSeq(
      Some(2),
      None,
      Some(0)))

  override def statusIndex = if (diverging) 1 else 0

  override def changeStatus: BlockStatus = SwitchStatus(block, trainId, lockedJunctions, !diverging)

  override def changeFreedom: BlockStatus = SwitchStatus(block, trainId, lockedJunctions.map(!_), diverging)

  /** Returns the end junction given the entry */
  override def junctionFrom = junctions(statusIndex)

  /** Returns the transit train in a junction */
  override def transitTrain = x => x match {
    case 0 => trainId
    case 1 => if (diverging) None else trainId
    case 2 => if (diverging) trainId else None
  }

  /** Creates a new block status applying trainId to a junction. */
  override def apply(junction: Int, trainId: Option[String]) = (diverging, junction) match {
    case (false, 0) if (trainId != this.trainId) => SwitchStatus(block, trainId, lockedJunctions, diverging)
    case (false, 1) if (trainId != this.trainId) => SwitchStatus(block, trainId, lockedJunctions, diverging)
    case (true, 0) if (trainId != this.trainId) => SwitchStatus(block, trainId, lockedJunctions, diverging)
    case (true, 2) if (trainId != this.trainId) => SwitchStatus(block, trainId, lockedJunctions, diverging)
    case _ => this
  }

  /** Returns the status with no transit train */
  override def noTrainStatus = if (trainId.isEmpty) this else SwitchStatus(block, None, lockedJunctions, diverging)

}
