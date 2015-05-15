package org.mmarini.railways3d.model

import com.jme3.math.Transform

/**
 *
 */
object TestStation {

  private val entry = EntryTemplate("entry")
  private val exit = ExitTemplate("exit")
  private val platform = PlatformTemplate("platform1")

  private val junctsions = Set(
    ((entry, 0), (platform, 0)),
    ((exit, 0), (platform, 1)))

  val topology = Topology(junctsions)
}