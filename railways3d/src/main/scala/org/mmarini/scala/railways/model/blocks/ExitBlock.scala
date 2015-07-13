/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** The exiting trains go out this BlockTemplate */
case class ExitBlock(id: String, trans: Transform2d) extends Block {

  /** Returns the current tracks group for a given track */
  def trackGroupFor: Int => Track => Set[Track] = _ => _ => Set()

  /** Returns the start junction containing a track */
  def junctionsForTrack: Int => Track => (Option[Int], Option[Int]) = _ => _ => (Some(0), None)

  /** Returns the track list for a junction */
  def tracksForJunction: Int => Int => (Option[Int], IndexedSeq[Track]) = _ => _ => (None, IndexedSeq())
}

/** Factory of [[ExitBlock]] */
object ExitBlock {
  
  /** Creates a [[ExitBlock]] */
  def apply(id: String, x: Float, y: Float, orientation: Float): ExitBlock =
    ExitBlock(id, Transform2d(x, y, orientation))
}
