/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import com.jme3.math.Vector2f

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
trait TrackBlock {
  def trackGroups: IndexedSeq[Set[Set[Track]]]
  def tracksForJunction: IndexedSeq[IndexedSeq[(Option[Int], IndexedSeq[Track])]]

  /** Returns the tracks group for a given configuration and a track and */
  def trackGroupFor: Int => Track => Set[Track] = (config) => (track) =>
    trackGroups(config).find(_.contains(track)) match {
      case Some(s) => s
      case None => Set.empty
    }

  /** Returns the junctions pair containing a track */
  def junctionsForTrack: Int => Track => (Option[Int], Option[Int]) = (config) => (track) => {
    val x = tracksForJunction(config).zipWithIndex
    val y = x.find {
      case ((to, trackList), from) => trackList.contains(track)
      case _ => false
    }
    y match {
      case Some(((to, trackList), from)) => (Some(from), to)
      case _ => (None, None)
    }
  }

}   
