/**
 *
 */
package org.mmarini.railways3d.model

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

  val Length: Float = SegmentLength * 11

  private val _paths = IndexedSeq(IndexedSeq(LinearTrack))

  /** Platform has only a single routing */
  override def routes(s: RoutingStatus): IndexedSeq[Route] = IndexedSeq()
}
