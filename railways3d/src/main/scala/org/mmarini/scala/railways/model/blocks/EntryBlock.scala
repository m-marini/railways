/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.tracks.HiddenTrack
import org.mmarini.scala.railways.model.Transform2d

/** The incoming trains come in this BlockTemplate */
case class EntryBlock(id: String, trans: Transform2d) extends Block with TrackBlock {

  val trackGroups: IndexedSeq[Set[Set[Track]]] = IndexedSeq(Set(Set(HiddenTrack)))

  val routes: IndexedSeq[Set[(Option[Int], Option[Int], IndexedSeq[Track])]] =
    IndexedSeq(Set((None, Some(0), IndexedSeq(HiddenTrack))))
}

/** Factory of [[EntryBlock]] */
object EntryBlock {

  /** Creates an [[EntryBlock]] */
  def apply(id: String, x: Float, y: Float, orientation: Float): EntryBlock =
    EntryBlock(id, Transform2d(x, y, orientation))
}