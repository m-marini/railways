/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus of switch */
case class SwitchStatus(
    block: SwitchBlock,
    transitTrain: Option[String] = None,
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

  override def changeStatus: BlockStatus = SwitchStatus(block, transitTrain, locked, !diverging)

  override def changeFreedom: BlockStatus = SwitchStatus(block, transitTrain, !locked, diverging)

  override def busy = !transitTrain.isEmpty

  /** Returns the end junction given the entry */
  override def junctionFrom = junctions(statusIndex)
}
