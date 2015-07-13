/**
 *
 */
package org.mmarini.scala.railways.model

/**
 * A block topology with the current status
 */

/** A [[BlockStatus]] of an entry [[Block]] */
case class EntryStatus(block: EntryBlock) extends BlockStatus
