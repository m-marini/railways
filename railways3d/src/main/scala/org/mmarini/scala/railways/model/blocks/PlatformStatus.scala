/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus]] of an exit [[Block]] */
case class PlatformStatus(
  block: PlatformBlock,
  trainId: Option[String] = None,
  lockedJunctions: IndexedSeq[Boolean] = IndexedSeq(false, false))
    extends SingleBlockStatus with LockableStatus {

  /** Toogles the status of block for a given index of status handler */
  override def lock = (j) => {
    require(j == 0 || j == 1)
    if (lockedJunctions(j))
      this
    else
      PlatformStatus(block, trainId, lockedJunctions.updated(j, true))
  }

  /** Toogles the status of block for a given index of status handler */
  override def unlock = (j) => {
    require(j == 0 || j == 1)
    if (lockedJunctions(j))
      PlatformStatus(block, trainId, lockedJunctions.updated(j, false))
    else
      this
  }

  /** Toogles the status of block for a given index of status handler */
  override def lockTrack = (j) => {
    require(j == 0 || j == 1)
    PlatformStatus(block, trainId, IndexedSeq(true, true))
  }

  override def unlockTrack = (j) => {
    require(j == 0 || j == 1)
    PlatformStatus(block, trainId, IndexedSeq(false, false))
  }

  /** Returns the end junction given the entry */
  override val junctionFrom = IndexedSeq(Option(1), Option(0))

  /** Returns the transit train in a junction */
  override def transitTrain = j => {
    require(j == 0 || j == 1)
    trainId
  }

  /** Creates a new block status applying trainId to a junction. */
  override def apply(junction: Int, trainId: Option[String]) = {
    require(junction == 0 || junction == 1)
    if (trainId != this.trainId)
      PlatformStatus(block, trainId, lockedJunctions)
    else
      this
  }

  /** Returns the status with no transit train */
  override def noTrainStatus = if (trainId.isEmpty) this else PlatformStatus(block, None, lockedJunctions)

  /** Returns the current identifiers of elements and the selection identifiers */
  override def elementIds = {
    val track = BlockElementIds(s"$id", "Textures/blocks/seg-track.blend", Some(s"track $id 0"))
    val jElements = for (junction <- 0 to 1) yield if (isClear(junction)) {
      BlockElementIds(
        s"$id $junction green",
        s"Textures/blocks/seg-green-$junction.blend",
        Some(s"junction $id $junction"))
    } else {
      BlockElementIds(
        s"$id $junction red",
        s"Textures/blocks/seg-red-$junction.blend",
        Some(s"junction $id $junction"))
    }
    jElements.toSet + track
  }

}
