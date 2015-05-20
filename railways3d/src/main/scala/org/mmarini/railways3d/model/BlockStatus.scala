package org.mmarini.railways3d.model

/**
 * A block topology with the current status
 */
trait BlockStatus {
  /** Returns the block topology */
  def block: Block
}

/** A [[BlockStatus]] of an entry [[Block]] */
case class EntryStatus(block: Block) extends BlockStatus

/** A [[BlockStatus]] of an exit [[Block]] */
case class ExitStatus(block: Block, busy: Boolean) extends BlockStatus

/** A [[BlockStatus]] of an exit [[Block]] */
case class PlatformStatus(block: Block, busy: Boolean) extends BlockStatus