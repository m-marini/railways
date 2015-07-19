/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus]] of an exit [[Block]] */
case class SegmentStatus(
    block: SegmentBlock,
    trainId: Option[String] = None,
    locked: Boolean = false) extends SingleBlockStatus with LockableStatus {

  override def busy: Boolean = !trainId.isEmpty

  /** */
  override def changeFreedom: BlockStatus = SegmentStatus(block, trainId, !locked)

  /** Returns the end junction given the entry */
  override val junctionFrom = IndexedSeq(Option(1), Option(0))

  /** Returns the transit train in a junction */
  override def transitTrain = _ => trainId
}