package org.mmarini.scala.railways.model

/**
 * @author us00852
 */
trait Block {
  /** Returns the identifier of block */
  def id: String

  /** Returns the transformation */
  def trans: Transform2d

  /** Returns the current tracks group for a given track */
  def trackGroupFor: Int => Track => Set[Track]

  /** Returns the start junction containing a track */
  def junctionsForTrack: Int => Track => (Option[Int], Option[Int])

  /** Returns the track list for a junction */
  def tracksForJunction: Int => Int => (Option[Int], IndexedSeq[Track])

}