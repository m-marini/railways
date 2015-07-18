/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus]] of an exit [[Block]] */
case class SegmentStatus(
    block: SegmentBlock,
    transitTrain: Option[String] = None,
    locked: Boolean = false) extends SingleBlockStatus with LockableStatus {

  override def busy: Boolean = !transitTrain.isEmpty

  /** */
  override def changeFreedom: BlockStatus = SegmentStatus(block, transitTrain, !locked)

  /** Returns the end junction given the entry */
  override val junctionFrom = IndexedSeq(Option(1), Option(0))
}