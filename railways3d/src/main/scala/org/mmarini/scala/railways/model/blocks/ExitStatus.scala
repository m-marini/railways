/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus]] of an exit [[Block]] */
case class ExitStatus(
    block: ExitBlock,
    trainId: Option[String] = None,
    locked: Boolean = false) extends SingleBlockStatus {

  /** */
  override def toogleLock = (j) => {
    require(j == 0)
    ExitStatus(block, trainId, !locked)
  }

  /** Returns None */
  override def junctionFrom = (j) => {
    require(j == 0)
    Some(1)
  }

  /** Returns the transit train in a junction */
  override def transitTrain = j => {
    require(j == 0)
    trainId
  }

  /** Creates a new block status applying trainId to a junction. */
  override def apply(junction: Int, trainId: Option[String]) = {
    require(junction == 0)
    if (trainId != this.trainId)
      ExitStatus(block, trainId)
    else
      this
  }

  /** Returns the status with no transit train */
  override def noTrainStatus: BlockStatus = if (trainId.isEmpty) this else ExitStatus(block, None, locked)

  /** Returns true if the junction is clear. */
  override def isClear = (j) => {
    require(j == 0)
    !locked && transitTrain(j).isEmpty
  }

  /** Returns the current identifiers of elements and the selection identifiers */
  override def elementIds =
    if (isClear(0)) {
      Set(BlockElementIds(s"$id green", "Textures/blocks/sem-green.blend", Some(s"junction $id 0")))
    } else {
      Set(BlockElementIds(s"$id red", "Textures/blocks/sem-red.blend", Some(s"junction $id 0")))
    }

}
