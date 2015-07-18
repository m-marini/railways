/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus]] of an exit [[Block]] */
case class PlatformStatus(
    block: PlatformBlock,
    transitTrain: Option[String] = None,
    locked: Boolean = false) extends SingleBlockStatus with LockableStatus {

  /** */
  override def changeFreedom: BlockStatus = PlatformStatus(block, transitTrain, !locked)

  /** Returns true if block is busy */
  override def busy: Boolean = !transitTrain.isEmpty

  /** Returns the end junction given the entry */
  override val junctionFrom = IndexedSeq(Option(1), Option(0))
}
