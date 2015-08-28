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
  override def junctionFrom: Int => Some[Int] = (j) => {
    require(j == 0)
    Some(1)
  }

  /** Returns the transit train in a junction */
  override def transitTrain: Int => Option[String] = j => {
    require(j == 0)
    trainId
  }

  /** Creates a new block status applying trainId to a junction. */
  override def apply(junction: Int, trainId: Option[String]): BlockStatus = {
    require(junction == 0)
    if (trainId != this.trainId) EntryStatus(block, trainId) else this
  }

  /** Returns the status with no transit train */
  override def noTrainStatus: BlockStatus = if (trainId.isEmpty) this else EntryStatus(block, None)

  /** Returns true if the junction is clear */
  override def isClear: Int => Boolean = j => {
    require(j == 0)
    false
  }

  /** Returns the current identifiers of elements and the selection identifiers */
  override val elementIds = Set(
    BlockElementIds(id, "Textures/blocks/sem-red.blend", None))
}
