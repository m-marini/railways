/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus]] of an exit [[Block]] */
case class SegmentStatus(block: SegmentBlock, busy: Boolean, locked: Boolean) extends BlockStatus with LockableStatus {

  /** */
  override def changeFreedom: BlockStatus = SegmentStatus(block, busy, !locked)

  /** Returns the current tracks group for a given track */
  override def trackGroupFor(track: Track): Set[Track] = {
    ???
  }

}
