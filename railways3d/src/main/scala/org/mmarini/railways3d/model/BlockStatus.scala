package org.mmarini.railways3d.model

/**
 * A status of block contains the identifier, block topology and the current status
 */
trait BlockStatus {
  def id: String
  def block: Block
}