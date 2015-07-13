/**
 *
 */
package org.mmarini.scala.railways.model

/**
 * A block topology with the current status
 */
trait BlockStatus {
  /** Returns the block topology */
  def block: Block

  def changeStatus: BlockStatus = this

  def changeFreedom: BlockStatus = this

  /** Returns the current tracks group for a given track */
  def trackGroupFor(track: Track): Set[Track] = ???

  /**
   * Creates a new block status applying a set of busy track.
   * The new status has to reflect the properties of blocked junctions on the given busy tracks
   */
  def apply(busyTracks: Set[Track]): BlockStatus = ??? // TODO

  /** Returns the start junction containing a track */
  def junctionsForTrack(track: Track): (Option[Int], Option[Int]) = ??? // TODO

  /** Returns the track list for a junction */
  def tracksForJunction(junction: Int): IndexedSeq[Track] = ??? // TODO
}

object BlockStatus {
  /** Creates the initial exit block status */
  def exit(block: ExitBlock): ExitStatus = ExitStatus(block, false, false)

  /** Creates the initial platform block status */
  def platform(block: PlatformBlock): PlatformStatus = PlatformStatus(block, false, false)

  /** Creates the initial platform block status */
  def segment(block: SegmentBlock): SegmentStatus = SegmentStatus(block, false, false)

  /** Creates the initial platform block status */
  def switch(block: SwitchBlock): SwitchStatus = SwitchStatus(block, false, false, false)
}

