/**
 *
 */
package org.mmarini.railways3d.model

/**
 * @author us00852
 *
 */
case class GameParameters(
  stationName: String,
  levelName: String,
  durationName: String,
  duration: Float,
  trainFrequence: Float,
  autoLock: Boolean,
  mute: Boolean,
  volume: Float) {
}

