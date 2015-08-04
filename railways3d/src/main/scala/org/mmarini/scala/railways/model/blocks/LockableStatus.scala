package org.mmarini.scala.railways.model.blocks

/**
 * @author us00852
 */
trait LockableStatus extends BlockStatus {

  /** Returns true if a junction is locked */
  def lockedJunctions: Int => Boolean

  /** Returns true if a junction is clear */
  override def isClear = (x) => !lockedJunctions(x) && transitTrain(x).isEmpty
}