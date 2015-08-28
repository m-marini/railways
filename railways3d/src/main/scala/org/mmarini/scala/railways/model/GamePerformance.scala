/**
 *
 */
package org.mmarini.scala.railways.model

/**
 * @author us00852
 */
case class GamePerformance(
    arrivals: Int = 0,
    departures: Int = 0,
    errors: Int = 0,
    elapsedTime: Float = 0) {

  /** Adds a value to the enteredTrainCount */
  def addElapsedTime(dt: Float): GamePerformance =
    GamePerformance(arrivals, departures, errors, elapsedTime + dt)

  /** Adds a value to the enteredTrainCount */
  def addArrivals(x: Int): GamePerformance =
    GamePerformance(arrivals + x, departures, errors, elapsedTime)

  /** Adds a value to the rightRoutedTrainCount */
  def addErrors(x: Int): GamePerformance =
    GamePerformance(arrivals, departures, errors + x, elapsedTime)

  /** Adds a value to the exitedTrainCount */
  def addDepartures(x: Int): GamePerformance =
    GamePerformance(arrivals, departures + x, errors, elapsedTime)
}
