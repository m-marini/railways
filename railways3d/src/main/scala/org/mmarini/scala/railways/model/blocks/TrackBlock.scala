/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
trait TrackBlock {
  def trackGroups: IndexedSeq[Set[Set[Track]]]
  def routes: IndexedSeq[Set[(Option[Int], Option[Int], IndexedSeq[Track])]]

  /** Returns the tracks group for a given configuration and a track and */
  def trackGroupFor: Int => Track => Set[Track] = (config) => (track) =>
    trackGroups(config).find(_.contains(track)) match {
      case Some(s) => s
      case None => Set.empty
    }

  /** Returns the junctions pair containing a track */
  def junctionsForTrack: Int => Track => (Option[Int], Option[Int]) = (config) => (track) =>
    routes(config).find {
      case (_, _, list) => list.contains(track)
    } match {
      case Some((from, to, _)) => (from, to)
      case _ => (None, None)
    }

  /** Returns the track list for a junction */
  def tracksForJunction: Int => Int => IndexedSeq[Track] = (config) => (from) =>
    routes(config).find(_._1.contains(from)) match {
      case Some((_, _, list)) => list
      case _ => IndexedSeq()
    }
}   
