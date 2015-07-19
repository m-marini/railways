/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus of switch */
case class SwitchStatus(
    block: SwitchBlock,
    trainId: Option[String] = None,
    locked: Boolean = false,
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

  override def changeStatus: BlockStatus = SwitchStatus(block, trainId, locked, !diverging)

  override def changeFreedom: BlockStatus = SwitchStatus(block, trainId, !locked, diverging)

  override def busy = !trainId.isEmpty

  /** Returns the end junction given the entry */
  override def junctionFrom = junctions(statusIndex)

  /** Returns the transit train in a junction */
  override def transitTrain = x => x match {
    case 0 => trainId
    case 1 => if (diverging) None else trainId
    case 2 => if (diverging) trainId else None
  }

}
