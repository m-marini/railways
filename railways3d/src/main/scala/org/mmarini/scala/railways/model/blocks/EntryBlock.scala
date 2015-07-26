/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.tracks.HiddenTrack
import org.mmarini.scala.railways.model.Transform2d

/** The incoming trains come in this BlockTemplate */
case class EntryBlock(id: String, trans: Transform2d) extends Block with TrackBlock {
  val entryTrack = new HiddenTrack

  override val trackGroups = IndexedSeq(Set(Set(entryTrack.asInstanceOf[Track])))

  override val routes =
    IndexedSeq(
      Set(
        (0, 1, IndexedSeq(entryTrack.asInstanceOf[Track]))))
}

/** Factory of [[EntryBlock]] */
object EntryBlock {

  /** Creates an [[EntryBlock]] */
  def apply(id: String, x: Float, y: Float, orientation: Float): EntryBlock =
    EntryBlock(id, Transform2d(x, y, orientation))
}