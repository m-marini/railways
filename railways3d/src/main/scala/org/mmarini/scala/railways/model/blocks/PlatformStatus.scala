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

  /** */
  override def toogleLock = (j) =>
    PlatformStatus(block, trainId, lockedJunctions.updated(j, !lockedJunctions(j)))

  /** Returns the end junction given the entry */
  override val junctionFrom = IndexedSeq(Option(1), Option(0))

  /** Returns the transit train in a junction */
  override def transitTrain = _ => trainId

  /** Creates a new block status applying trainId to a junction. */
  override def apply(junction: Int, trainId: Option[String]) =
    if ((junction == 0 || junction == 1) && trainId != this.trainId) PlatformStatus(block, trainId, lockedJunctions)
    else this

  /** Returns the status with no transit train */
  override def noTrainStatus = if (trainId.isEmpty) this else PlatformStatus(block, None, lockedJunctions)

  /** Create a block status with a given locked junction */
  override def lock = (j) =>
    if (lockedJunctions(j))
      this
    else
      PlatformStatus(block, trainId, lockedJunctions.updated(j, true))

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
