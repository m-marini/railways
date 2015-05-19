/**
 *
 */
package org.mmarini.railways3d.model

/**
 * A template for block producing routes depending on status of block
 */
trait BlockTemplate {

  /** Returns the routes depending on the status */
  def routes(s: RoutingStatus): IndexedSeq[Route]
}

/** The incoming trains arrive from this BlockTemplate */
case object Entry extends BlockTemplate {
  /** Entry has no routing */
  def routes(s: RoutingStatus) = IndexedSeq()
}

/** The exiting trains go to this BlockTemplate */
case object Exit extends BlockTemplate {
  /** Exit has no routing  */
  def routes(s: RoutingStatus) = IndexedSeq()
}

/** The arriving trains stop at this BlockTemplate to wait passengers boarding */
case object Platform extends BlockTemplate {

  val Length: Float = SegmentLength * 11

  private val _paths = IndexedSeq(IndexedSeq(LinearTrack))

  /** Platform has only a single routing */
  def routes(s: RoutingStatus) = IndexedSeq()
} 