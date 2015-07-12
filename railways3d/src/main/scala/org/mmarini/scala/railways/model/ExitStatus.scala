/**
 *
 */
package org.mmarini.scala.railways.model

/** A [[BlockStatus]] of an exit [[Block]] */
case class ExitStatus(block: Block, busy: Boolean, locked: Boolean) extends BlockStatus with LockableStatus {

  /** */
  override def changeFreedom: BlockStatus = ExitStatus(block, busy, !locked)
}
