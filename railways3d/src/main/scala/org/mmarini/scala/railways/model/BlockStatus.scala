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

  def changeStatus: BlockStatus
}

/** A [[BlockStatus]] of an entry [[Block]] */
case class EntryStatus(block: Block) extends BlockStatus {
  def changeStatus: BlockStatus = this
}

/** A [[BlockStatus]] of an exit [[Block]] */
case class ExitStatus(block: Block, busy: Boolean) extends BlockStatus {
  def changeStatus: BlockStatus = this
}

/** A [[BlockStatus]] of an exit [[Block]] */
case class PlatformStatus(block: Block, busy: Boolean) extends BlockStatus {
  def changeStatus: BlockStatus = this
}

/** A [[BlockStatus of deviator */
case class DeviatorStatus(block: Block, busy: Boolean, deviated: Boolean) extends BlockStatus {
  def changeStatus: BlockStatus = DeviatorStatus(block, busy, !deviated)
}

