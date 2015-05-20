/**
 *
 */
package org.mmarini.railways3d.model

/** A set of game parameters */
case class GameParameters(
  stationName: String = "",
  levelName: String = "",
  durationName: String = "",
  duration: Float = 1f,
  trainFrequence: Float = 1f,
  autoLock: Boolean = true,
  mute: Boolean = false,
  volume: Float = 0.5f)
  