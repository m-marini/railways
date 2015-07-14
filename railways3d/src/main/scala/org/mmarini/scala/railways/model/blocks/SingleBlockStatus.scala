/**
 *
 */
package org.mmarini.scala.railways.model.blocks

/**
 * A block topology with the current status
 */
trait SingleBlockStatus extends IndexedBlockStatus {
  /** Returns the index of status */
  override val statusIndex = 0
}
