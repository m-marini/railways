/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/**
 * A block topology with the current status
 */
trait BlockStatus {
  /** Returns the block topology */
  def block: Block

  /** Returns block id */
  def id: String = block.id

  /** Returns the current identifiers of elements and the selection identifiers */
  def elementIds: Set[BlockElementIds] = Set()

  /** Toogles the status of block for a given index of status handler */
  def toogleStatus: Int => BlockStatus = (_) => this

  /** Toogles the locking status for a given junction */
  def toogleLock: Int => BlockStatus = (_) => this

  /** Create a block status with a given locked junction */
  def lock: Int => BlockStatus = (_) => this

  /**
   * Creates a new block status applying trainId to a junction.
   * The new status has to reflect the properties of blocked junctions on the given busy tracks
   */
  def apply(junction: Int, trainId: Option[String]): BlockStatus

  /** Returns the start junction containing a track */
  def junctionsForTrack: Track => Option[(Int, Int)]

  /** Returns the track list for a junction */
  def tracksForJunction: Int => IndexedSeq[Track]

  /** Returns the end junction given the entry */
  def junctionFrom: Int => Option[Int]

  /** Returns the train running from a junction */
  def transitTrain: Int => Option[String]

  /** Returns the status with no transit train */
  def noTrainStatus: BlockStatus

  /**
   * Returns true if the junction is clear
   * The junction is clear if tracks exists to next junction
   * and no train is running or crossing the tracks from it
   * and if the junction has not been locked
   * The home semaphore of junction is green if the junction is clear else it is red
   */
  def isClear: Int => Boolean
}
