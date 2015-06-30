/**
 *
 */
package org.mmarini.scala.railways.model

/**
 * A block topology with the current status
 */
trait BlockStatus {
  /** Returns the block topology */
  def block: Block

  def changeStatus: BlockStatus = this

  def changeFreedom: BlockStatus = this
}

/** A [[BlockStatus]] of an entry [[Block]] */
case class EntryStatus(block: Block) extends BlockStatus

trait LockableStatus {

  /** */
  def busy: Boolean

  /** */
  def locked: Boolean

  /** */
  def free: Boolean = !busy && !locked
}

/** A [[BlockStatus]] of an exit [[Block]] */
case class ExitStatus(block: Block, busy: Boolean, locked: Boolean) extends BlockStatus with LockableStatus {

  /** */
  override def changeFreedom: BlockStatus = ExitStatus(block, busy, !locked)
}

/** A [[BlockStatus]] of an exit [[Block]] */
case class PlatformStatus(block: Block, busy: Boolean, locked: Boolean) extends BlockStatus with LockableStatus {

  /** */
  override def changeFreedom: BlockStatus = PlatformStatus(block, busy, !locked)
}

/** A [[BlockStatus]] of an exit [[Block]] */
case class SegmentStatus(block: Block, busy: Boolean, locked: Boolean) extends BlockStatus with LockableStatus {

  /** */
  override def changeFreedom: BlockStatus = SegmentStatus(block, busy, !locked)
}

/** A [[BlockStatus of switch */
case class SwitchStatus(block: Block, busy: Boolean, locked: Boolean, diverging: Boolean) extends BlockStatus with LockableStatus {

  override def changeStatus: BlockStatus = SwitchStatus(block, busy, locked, !diverging)

  override def changeFreedom: BlockStatus = SwitchStatus(block, busy, !locked, diverging)
}

object BlockStatus {
  /** Creates the initial exit block status */
  def exit(block: Block): ExitStatus = ExitStatus(block, false, false)

  /** Creates the initial platform block status */
  def platform(block: Block): PlatformStatus = PlatformStatus(block, false, false)

  /** Creates the initial platform block status */
  def segment(block: Block): SegmentStatus = SegmentStatus(block, false, false)

  /** Creates the initial platform block status */
  def switch(block: Block): SwitchStatus = SwitchStatus(block, false, false, false)
}

