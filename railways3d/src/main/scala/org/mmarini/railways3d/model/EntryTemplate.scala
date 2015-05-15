/**
 *
 */
package org.mmarini.railways3d.model

import com.jme3.math.Transform

/**
 * @author us00852
 *
 */
object EntryTemplate {
  private val template = new BlockTemplate("entry", IndexedSeq(IndexedSeq()))

  def apply(id: String): Block = Block(id, template, new Transform)
}