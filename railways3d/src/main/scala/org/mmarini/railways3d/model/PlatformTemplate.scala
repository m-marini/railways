package org.mmarini.railways3d.model

import scala.collection.IndexedSeq
import com.jme3.math.Transform

/**
 *
 */
object PlatformTemplate {
  private val track = new LinearTrack()

  private val template = BlockTemplate("platform", IndexedSeq(IndexedSeq(track)))

  def apply(id: String): Block = Block(id, template, new Transform)
}