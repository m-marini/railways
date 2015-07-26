/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.tracks.HiddenTrack
import org.mmarini.scala.railways.model.Transform2d

/** The exiting trains go out this BlockTemplate */
case class ExitBlock(id: String, trans: Transform2d) extends Block with TrackBlock {

  val track = new HiddenTrack

  override val trackGroups = IndexedSeq(Set(Set(track.asInstanceOf[Track])))

  override val routes = IndexedSeq(
    Set(
      (0, 1, IndexedSeq(track.asInstanceOf[Track]))))
}

/** Factory of [[ExitBlock]] */
object ExitBlock {

  /** Creates a [[ExitBlock]] */
  def apply(id: String, x: Float, y: Float, orientation: Float): ExitBlock =
    ExitBlock(id, Transform2d(x, y, orientation))
}
