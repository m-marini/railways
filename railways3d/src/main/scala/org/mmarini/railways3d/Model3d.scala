/**
 *
 */
package org.mmarini.railways3d

import com.jme3.scene.Spatial
import org.mmarini.railways3d.model.Block

/**
 * It stores the spatial models of the concrete block.
 *
 * Each block has more 3d models depending on the status of the block
 * (e.g. red semaphore or green semaphore, busy track or free track)
 *
 */
trait Model3d {
  /** */
  def apply(status: ModelStatus): Model3d
}

trait ModelStatus

object ModelStatus {

  private case object EntryStatus extends ModelStatus

  /** Extracts the status of a block  */
  def apply(b: Block): ModelStatus = ???
}
