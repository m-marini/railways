/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq

/**
 * Produces routes depending on status of block
 */
trait BlockTemplate {

  /** Returns the routes depending on the status */
  def routes(s: RoutingStatus): IndexedSeq[Route]
}

/** The incoming trains come in this BlockTemplate */
case object Entry extends BlockTemplate {
  /** Entry has no routing */
  def routes(s: RoutingStatus): IndexedSeq[Route] = IndexedSeq()
}

/** The exiting trains go out this BlockTemplate */
case object Exit extends BlockTemplate {
  /** Exit has no routing  */
  def routes(s: RoutingStatus): IndexedSeq[Route] = IndexedSeq()
}

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
case object Platform extends BlockTemplate {
  private val _paths = IndexedSeq(IndexedSeq(LinearTrack))

  /** Platform has only a single routing */
  override def routes(s: RoutingStatus): IndexedSeq[Route] = IndexedSeq()
}

/** The arriving trains stop at this BlockTemplate to wait for passengers boarding */
case object TrackTemplate extends BlockTemplate {
  private val _paths = IndexedSeq(IndexedSeq(LinearTrack))

  /** Platform has only a single routing */
  override def routes(s: RoutingStatus): IndexedSeq[Route] = IndexedSeq()
}

case object LeftDeviator extends BlockTemplate {

  /** Deviator has two routing */
  override def routes(s: RoutingStatus): IndexedSeq[Route] = IndexedSeq()

}

case object RightDeviator extends BlockTemplate {

  /** Deviator has two routing */
  override def routes(s: RoutingStatus): IndexedSeq[Route] = IndexedSeq()

}
