/**
 *
 */
package org.mmarini.railways3d

import com.jme3.scene.Spatial
import org.mmarini.railways3d.model.Block
import org.mmarini.railways3d.model.BlockStatus
import com.jme3.scene.Node

/**
 * A model containing the current jme3 model of a block
 *
 * Generates new BlockModel applying a status to the model
 */
trait BlockModel3d {
  /** Returns the next 3d block model appropriate to the game block status */
  def apply(block: BlockStatus)(rootNode: Node): BlockModel3d
}

object BlockModelTemplates {
  
}