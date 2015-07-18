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
  transitTrain: Option[String] = None)
    extends BlockStatus with SingleBlockStatus {

  /** Returns None */
  override def junctionFrom = (_) => None
}
