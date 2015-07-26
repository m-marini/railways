/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/**
 * A block topology with the current status
 */
trait IndexedBlockStatus extends BlockStatus {
  /** Returns the index of status */
  def statusIndex: Int

  /** Returns the current tracks group for a given track */
  def trackGroupFor: Track => Set[Track] = block.trackGroupFor(statusIndex)

  /** Returns the start junction containing a track */
  override def junctionsForTrack: Track => Option[(Int, Int)] = block.junctionsForTrack(statusIndex)

  /** Returns the track list for a junction */
  def tracksForJunction: Int => IndexedSeq[Track] = block.tracksForJunction(statusIndex)
}
