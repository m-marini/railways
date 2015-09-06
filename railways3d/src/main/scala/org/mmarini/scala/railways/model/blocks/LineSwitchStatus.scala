/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track
import com.typesafe.scalalogging.LazyLogging

object LineSwitchConfiguration extends Enumeration {
  type Configuration = Value
  val BothStraight, RightSwitched, LeftSwitched, BothSwitched = Value
}

import LineSwitchConfiguration._

/** A [[LineSwitchStatus]] of switch */
case class LineSwitchStatus(
  block: LineSwitchBlock,
  trainId: IndexedSeq[Option[String]] = (0 to 3).map(_ => None),
  lockedJunctions: IndexedSeq[Boolean] = (0 to 3).map(_ => true),
  statusIndex: Int = BothStraight.id)
    extends IndexedBlockStatus
    with LockableStatus
    with LazyLogging {

  /** Transition table (configuration => handler => new configuration*/
  private val statusTransition = IndexedSeq(
    IndexedSeq(1, 2),
    IndexedSeq(0, 3),
    IndexedSeq(3, 0),
    IndexedSeq(2, 1))

  private val junctions = IndexedSeq(
    IndexedSeq(Some(2), Some(3), Some(0), Some(1)),
    IndexedSeq(Some(3), None, None, Some(0)),
    IndexedSeq(None, Some(2), Some(1), None),
    IndexedSeq(Some(2), None, Some(0), None))

  /** Toogles the status of block for a given index of status handler */
  override def toogleStatus: Int => BlockStatus = (h) => {
    require(h >= 0 && h <= 3)
    // Check if there are no trains
    if (trainId.forall(_.isEmpty)) {
      LineSwitchStatus(block, trainId, lockedJunctions, statusTransition(statusIndex)(h))
    } else {
      this
    }
  }

  override def lock: Int => BlockStatus = (j) => {
    require(j >= 0 && j <= 3)
    if (lockedJunctions(j)) {
      this
    } else {
      LineSwitchStatus(block, trainId, lockedJunctions.updated(j, true), statusIndex)
    }
    this
  }

  override def unlock: Int => BlockStatus = (j) => {
    require(j >= 0 && j <= 3)
    if (lockedJunctions(j)) {
      LineSwitchStatus(block, trainId, lockedJunctions.updated(j, false), statusIndex)
    } else {
      this
    }
  }

  /** Create a block status with a given locked junction */
  override def lockTrack: Int => BlockStatus = (j) => {
    require(j >= 0 && j <= 3)
    val junctions = junctionFrom(j).toSeq :+ j
    if (junctions.forall(x => lockedJunctions(x))) {
      this
    } else {
      val locked2 = junctions.foldLeft(lockedJunctions)((l, j) => l.updated(j, true))
      LineSwitchStatus(block, trainId, locked2, statusIndex)
    }
  }

  /** Toogles the status of block for a given index of status handler */
  override def unlockTrack: Int => BlockStatus = (j) => {
    require(j >= 0 && j <= 3)
    val junctions = junctionFrom(j).toSeq :+ j
    if (junctions.forall(x => !lockedJunctions(x))) {
      this
    } else {
      val locked2 = junctions.foldLeft(lockedJunctions)((l, j) => l.updated(j, false))
      LineSwitchStatus(block, trainId, locked2, statusIndex)
    }
  }

  /** Returns the end junction given the entry */
  override def junctionFrom: Int => Option[Int] = junctions(statusIndex)

  /** Returns the transit train in a junction */
  override def transitTrain: Int => Option[String] = j => trainId(j)

  /** Creates a new block status applying trainId to a junction. */
  override def apply(junction: Int, trainId: Option[String]): BlockStatus = {
    val junctions = junctionFrom(junction).toSeq :+ junction
    if (junctions.forall(x => this.trainId(x) == trainId)) {
      this
    } else {
      val trainId1 = junctions.foldLeft(this.trainId)((trains, j) => trains.updated(j, trainId))
      LineSwitchStatus(block = block,
        trainId = trainId1,
        lockedJunctions = lockedJunctions,
        statusIndex = statusIndex)
    }
  }

  /** Returns the status with no transit train */
  override def noTrainStatus: BlockStatus =
    if (trainId.forall(_.isEmpty)) {
      this
    } else {
      LineSwitchStatus(block = block,
        lockedJunctions = lockedJunctions,
        statusIndex = statusIndex)
    }

  /** Create a block status with a given locked junction */
  override def autolock: Int => BlockStatus = lockTrack

  /** Returns the current identifiers of elements and the selection identifiers */
  override def elementIds: Set[BlockElementIds] = {

    val semaphores = for (junction <- 0 to 4) yield if (isClear(junction)) {
      BlockElementIds(
        s"$id $junction green",
        s"Textures/blocks/lineswitch/sem-$junction-green.blend",
        Some(s"junction $id $junction"))
    } else {
      BlockElementIds(
        s"$id $junction red",
        s"Textures/blocks/lineswitch/sem-$junction-red.blend",
        Some(s"junction $id $junction"))
    }

    val tracks = for (conf <- LineSwitchConfiguration.values) yield BlockElementIds(
      s"$id ${conf.id}",
      s"Textures/blocks/swi-track-${conf.id}.blend",
      Some(s"track $id 0"))

    val handlers = for (handler <- 0 to 1) yield BlockElementIds(
      s"$id ${statusIndex} $handler handler",
      s"Textures/blocks/swi-hand-$handler-${statusIndex / (1 << handler) % 2}.blend",
      Some(s"handler $id $handler"))

    semaphores.toSet ++ tracks ++ handlers
  }

}
