/**
 *
 */
package org.mmarini.scala.railways.model.blocks

/**
 * 
 */
trait LockableStatus {

  /** */
  def busy: Boolean

  /** */
  def locked: Boolean

  /** */
  def free: Boolean = !busy && !locked
}
