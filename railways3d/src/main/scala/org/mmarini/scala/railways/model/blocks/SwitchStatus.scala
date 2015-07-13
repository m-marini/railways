/**
 *
 */
package org.mmarini.scala.railways.model.blocks

/** A [[BlockStatus of switch */
case class SwitchStatus(block: SwitchBlock, busy: Boolean, locked: Boolean, diverging: Boolean) extends BlockStatus with LockableStatus {

  override def changeStatus: BlockStatus = SwitchStatus(block, busy, locked, !diverging)

  override def changeFreedom: BlockStatus = SwitchStatus(block, busy, !locked, diverging)
}
