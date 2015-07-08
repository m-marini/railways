/**
 *
 */
package org.mmarini.scala.railways.model

/**
 * @author us00852
 *
 */
case class StationStatus(topology: Topology, blocks: Map[String, BlockStatus]) {

  /** Generates the next status changing the status of a block */
  def changeBlockStatus(id: String): StationStatus =
    setBlocks(blocks.
      get(id).
      map(_.changeStatus).
      map(bs => blocks + (id -> bs)).
      getOrElse(blocks))

  /** Generates the next status changing the freedom of a block */
  def changeBlockFreedom(id: String): StationStatus =
    setBlocks(blocks.
      get(id).
      map(_.changeFreedom).
      map(bs => blocks + (id -> bs)).
      getOrElse(blocks))

  /** Creates a new status with a new block map */
  def setBlocks(blocks: Map[String, BlockStatus]): StationStatus =
    StationStatus(topology, blocks)

  /** Finds the path with a given track */
  def findPath(from: Track): IndexedSeq[Track] = {
    ???
  }
}
