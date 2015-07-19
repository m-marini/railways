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

  def changeStatus: BlockStatus = this

  def changeFreedom: BlockStatus = this

  /** Returns the current tracks group for a given track */
  def trackGroupFor: Track => Set[Track]

  /**
   * Creates a new block status applying a set of busy track.
   * The new status has to reflect the properties of blocked junctions on the given busy tracks
   */
  def apply(busyTracks: Set[Track]): BlockStatus = ??? // TODO

  /** Returns the start junction containing a track */
  def junctionsForTrack: Track => (Option[Int], Option[Int])

  /** Returns the track list for a junction */
  def tracksForJunction: Int => IndexedSeq[Track]

  /** Returns the end junction given the entry */
  def junctionFrom: Int => Option[Int]

  /** Returns the transit train in a junction */
  def transitTrain: Int => Option[String]
}
