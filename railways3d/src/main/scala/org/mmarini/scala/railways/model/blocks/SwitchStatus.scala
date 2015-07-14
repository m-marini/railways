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

  override def statusIndex = if (diverging) 1 else 0

  override def changeStatus: BlockStatus = SwitchStatus(block, transitTrain, locked, !diverging)

  override def changeFreedom: BlockStatus = SwitchStatus(block, transitTrain, !locked, diverging)

  override def busy = !transitTrain.isEmpty
}
