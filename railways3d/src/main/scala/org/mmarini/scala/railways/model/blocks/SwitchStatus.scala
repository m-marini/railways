/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track
import com.typesafe.scalalogging.LazyLogging

/** A [[BlockStatus of switch */
case class SwitchStatus(
    block: SwitchBlock,
    trainId: Option[String] = None,
    lockedJunctions: IndexedSeq[Boolean] = IndexedSeq(false, false, false),
    diverging: Boolean = false) extends IndexedBlockStatus with LockableStatus with LazyLogging {

  private val junctions = IndexedSeq(
    IndexedSeq(
      Some(1),
      Some(0),
      None),
    IndexedSeq(
      Some(2),
      None,
      Some(0)))

  override def statusIndex = if (diverging) 1 else 0

  override def toogleStatus = (j) => {
    require(j >= 0 && j <= 2)
    if (trainId.isEmpty)
      SwitchStatus(block, trainId, lockedJunctions, !diverging)
    else
      this
  }

  override def lock = (j) => {
    require(j >= 0 && j <= 2)
    if (lockedJunctions(j))
      this
    else
      SwitchStatus(block, trainId, lockedJunctions.updated(j, true), diverging)
  }

  override def unlock = (j) => {
    require(j >= 0 && j <= 2)
    if (lockedJunctions(j))
      SwitchStatus(block, trainId, lockedJunctions.updated(j, false), diverging)
    else
      this
  }

  /** Returns the end junction given the entry */
  override def junctionFrom = junctions(statusIndex)

  /** Returns the transit train in a junction */
  override def transitTrain = j => j match {
    case 0 => trainId
    case 1 => if (diverging) None else trainId
    case 2 => if (diverging) trainId else None
  }

  /** Creates a new block status applying trainId to a junction. */
  override def apply(junction: Int, trainId: Option[String]) = (diverging, junction) match {
    case (false, 0) if (trainId != this.trainId) => SwitchStatus(block, trainId, lockedJunctions, diverging)
    case (false, 1) if (trainId != this.trainId) => SwitchStatus(block, trainId, lockedJunctions, diverging)
    case (true, 0) if (trainId != this.trainId) => SwitchStatus(block, trainId, lockedJunctions, diverging)
    case (true, 2) if (trainId != this.trainId) => SwitchStatus(block, trainId, lockedJunctions, diverging)
    case _ => this
  }

  /** Returns the status with no transit train */
  override def noTrainStatus = if (trainId.isEmpty) this else SwitchStatus(block, None, lockedJunctions, diverging)

  /** Create a block status with a given locked junction */
  override def autolock = (j) =>
    if (lockedJunctions(j))
      this
    else
      SwitchStatus(
        block = block,
        trainId = trainId,
        lockedJunctions = IndexedSeq(true, true, true),
        diverging = diverging)

  /** Returns the current identifiers of elements and the selection identifiers */
  override def elementIds = {
    val dir = if (block.isInstanceOf[LeftHandSwitchBlock]) "l" else "r"
    val st = if (diverging) "div" else "str"
    val jElements = for (junction <- 0 to 2) yield if (isClear(junction)) {
      BlockElementIds(
        s"$id $junction green",
        s"Textures/blocks/swi-$dir-$junction-green.blend",
        Some(s"junction $id $junction"))
    } else {
      BlockElementIds(
        s"$id $junction red",
        s"Textures/blocks/swi-$dir-$junction-red.blend",
        Some(s"junction $id $junction"))
    }
    jElements.toSet +
      BlockElementIds(
        s"$id $st handler",
        s"Textures/blocks/swi-$dir-$st-hand.blend",
        Some(s"handler $id 0")) +
        BlockElementIds(
          s"$id $st",
          s"Textures/blocks/swi-$dir-$st.blend",
          Some(s"track $id 0"))
  }

}
