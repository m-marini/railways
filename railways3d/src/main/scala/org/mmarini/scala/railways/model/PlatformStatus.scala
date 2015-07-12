/**
 *
 */
package org.mmarini.scala.railways.model

/** A [[BlockStatus]] of an exit [[Block]] */
case class PlatformStatus(block: Block, busy: Boolean, locked: Boolean) extends BlockStatus with LockableStatus {

  /** */
  override def changeFreedom: BlockStatus = PlatformStatus(block, busy, !locked)
}
