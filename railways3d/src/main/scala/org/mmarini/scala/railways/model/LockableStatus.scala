/**
 *
 */
package org.mmarini.scala.railways.model

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
