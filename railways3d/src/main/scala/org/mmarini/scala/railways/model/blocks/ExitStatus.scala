/**
 *
 */
package org.mmarini.scala.railways.model.blocks

import org.mmarini.scala.railways.model.tracks.Track

/** A [[BlockStatus]] of an exit [[Block]] */
case class ExitStatus(
    block: ExitBlock,
    trainId: Option[String] = None,
    locked: Boolean = false) extends SingleBlockStatus with LockableStatus {

  /** */
  override def changeFreedom = ExitStatus(block, trainId, !locked)

  override def busy = !trainId.isEmpty

  /** Returns None */
  override def junctionFrom = (_) => None

  /** Returns the transit train in a junction */
  override def transitTrain = _ => trainId

}
