/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.tracks.EntryTrack

/** The incoming trains come in this BlockTemplate */
case class EntryBlock(id: String, trans: Transform2d) extends Block {

  /** Returns the current tracks group for a given track */
  def trackGroupFor: Int => Track => Set[Track] = (config) => (t) => Set(t)

  /** Returns the start junction containing a track */
  def junctionsForTrack: Int => Track => (Option[Int], Option[Int]) = (config) => (track) =>
    (None, Some(0))

  /** Returns the track list for a junction */
  def tracksForJunction: Int => Int => (Option[Int], IndexedSeq[Track]) = (config) => (junction) =>
    (None, IndexedSeq(EntryTrack))
}

/** Factory of [[EntryBlock]] */
object EntryBlock {

  /** Creates an [[EntryBlock]] */
  def apply(id: String, x: Float, y: Float, orientation: Float): EntryBlock =
    EntryBlock(id, Transform2d(x, y, orientation))
}