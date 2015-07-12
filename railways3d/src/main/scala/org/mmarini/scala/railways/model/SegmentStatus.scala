/**
 *
 */
package org.mmarini.scala.railways.model

/** A [[BlockStatus]] of an exit [[Block]] */
case class SegmentStatus(block: Block, busy: Boolean, locked: Boolean) extends BlockStatus with LockableStatus {

  /** */
  override def changeFreedom: BlockStatus = SegmentStatus(block, busy, !locked)

  /** Returns the current tracks group for a given track */
  override def trackGroupFor(track: Track): Set[Track] = {
    ???
  }

}
