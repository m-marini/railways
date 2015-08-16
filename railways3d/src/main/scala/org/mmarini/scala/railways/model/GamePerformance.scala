package org.mmarini.scala.railways.model

/**
 * @author us00852
 */
case class GamePerformance(
    enteredTrainCount: Int = 0,
    exitedTrainCount: Int = 0,
    rightRoutedTrainCount: Int = 0,
    elapsedTime: Float = 0) {

  /** Adds a value to the enteredTrainCount */
  def addElapsedTime(dt: Float): GamePerformance =
    GamePerformance(enteredTrainCount, exitedTrainCount, rightRoutedTrainCount, elapsedTime + dt)

  /** Adds a value to the enteredTrainCount */
  def addEnteredTrainCount(x: Int): GamePerformance =
    GamePerformance(enteredTrainCount + x, exitedTrainCount, rightRoutedTrainCount, elapsedTime)

  /** Adds a value to the rightRoutedTrainCount */
  def addRightRoutedTrainCount(x: Int): GamePerformance =
    GamePerformance(enteredTrainCount, exitedTrainCount, rightRoutedTrainCount + x, elapsedTime)

  /** Adds a value to the exitedTrainCount */
  def addExitedTrainCount(x: Int): GamePerformance =
    GamePerformance(enteredTrainCount, exitedTrainCount + x, rightRoutedTrainCount, elapsedTime)
}