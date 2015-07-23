/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.tracks.HiddenTrack
import org.mmarini.scala.railways.model.Transform2d

/** The exiting trains go out this BlockTemplate */
case class ExitBlock(id: String, trans: Transform2d) extends Block with TrackBlock {

  val trackGroups: IndexedSeq[Set[Set[Track]]] = IndexedSeq(Set(Set(HiddenTrack)))

  val routes: IndexedSeq[Set[(Option[Int], Option[Int], IndexedSeq[Track])]] =
    IndexedSeq(Set((Some(0), None, IndexedSeq(HiddenTrack))))
}

/** Factory of [[ExitBlock]] */
object ExitBlock {

  /** Creates a [[ExitBlock]] */
  def apply(id: String, x: Float, y: Float, orientation: Float): ExitBlock =
    ExitBlock(id, Transform2d(x, y, orientation))
}
