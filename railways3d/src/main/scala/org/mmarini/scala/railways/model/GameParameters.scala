/**
 *
 */
package org.mmarini.scala.railways.model

/**
 * A set of game parameters:
 *
 * station: name of station
 * levelName: name of game level
 * durationName: name of duration fo game
 * duration: duration of game in seconds
 * trainFrequence: frequency of trains in train / seconds
 * autoLock: auto-lock of signals
 * mute: mute the audio
 * volume: set the volume
 */
case class GameParameters(
  stationName: String = "",
  levelName: String = "",
  durationName: String = "",
  trainFrequence: Float = 1f,
  duration: Float = 1f,
  autoLock: Boolean = true,
  mute: Boolean = false,
  volume: Float = 0.5f)
