/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/**
 * A block topology with the current status
 */

/** A [[BlockStatus]] of an entry [[Block]] */
case class EntryStatus(
  block: EntryBlock,
  trainId: Option[String] = None)
    extends BlockStatus with SingleBlockStatus {

  /** Returns None */
  override def junctionFrom = (x) => if (x == 0) Some(1) else None

  /** Returns the transit train in a junction */
  override def transitTrain = _ => trainId

  /** Creates a new block status applying trainId to a junction. */
  override def apply(junction: Int, trainId: Option[String]) = junction match {
    case 0 if (trainId != this.trainId) => EntryStatus(block, trainId)
    case _ => this
  }

  /** Returns the status with no transit train */
  override def noTrainStatus: BlockStatus = if (trainId.isEmpty) this else EntryStatus(block, None)

  /** Returns true if the junction is clear */
  override def isClear = _ => false
}
