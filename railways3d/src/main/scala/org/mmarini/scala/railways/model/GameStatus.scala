/**
 *
 */
package org.mmarini.scala.railways.model

import com.typesafe.scalalogging.LazyLogging
import scala.util.Random
import scala.math.E
import scala.math.exp
import scala.reflect.api.Position
import com.jme3.math.Vector3f
import com.jme3.math.Vector2f

/**
 * A set of game parameter, station [[Topology]], elapsed time and set of named [[BlockStatus]]
 *
 * Generates next status by handling the incoming events
 */
case class GameStatus(
  parameters: GameParameters,
  time: Float,
  stationStatus: StationStatus,
  random: Random,
  trains: Set[Train]) extends LazyLogging {

  def vehicles: Set[Vehicle] =
    for {
      train <- trains
      vehicle <- train.vehicles
    } yield vehicle

  /** Creates a new status with a new time value */
  def setTime(time: Float): GameStatus =
    GameStatus(parameters, time, stationStatus, random, trains)

  /** Creates a new status with time changed by a value */
  def addTime(dt: Float): GameStatus =
    GameStatus(parameters, time + dt, stationStatus, random, trains)

  /** Creates a new status with a new block map */
  def setStationStatus(stationStatus: StationStatus): GameStatus =
    GameStatus(parameters, time, stationStatus, random, trains)

  /** Creates a new status with a new train set */
  def setTrains(trains: Set[Train]): GameStatus =
    GameStatus(parameters, time, stationStatus, random, trains)

  /** Generates the next status simulating a time elapsing */
  def tick(time: Float): GameStatus = {
    // Generates status changes for each train e returns the final status
    val newStatus = trains.foldLeft(this)((status, train) => {
      // Processes single train
      val newTrain = train.tick(time, status)
      // Rebuilds the status for train change
      val newStatus = status.setTrains((trains - train) ++ newTrain.toSet)
      newStatus
    })

    // TODO generare nuovi treni 
    val newTrainStatus = newStatus

    newTrainStatus.addTime(time)
  }

  /** Generates a random integer with a Poisson distribution */
  private def poisson(lambda: Double): Int = {
    val l = exp(-lambda)

    def poissonLoop(k: Int, p: Double): Int = {
      val p1 = p * random.nextDouble
      if (p1 <= l) k else poissonLoop(k + 1, p1)
    }
    poissonLoop(0, 1)
  }

  /** Choices a random element with equal probability distribution */
  private def choice[T](choices: Set[T]): T = {
    val idx = random.nextInt(choices.size)
    choices.toSeq(idx)
  }

  /** Generates the next status changing the status of a block */
  def changeBlockStatus(id: String): GameStatus =
    setStationStatus(stationStatus.changeBlockStatus(id))

  /** Generates the next status changing the freedom of a block */
  def changeBlockFreedom(id: String): GameStatus =
    setStationStatus(stationStatus.changeBlockFreedom(id))

}

/** A factory of [[GameStatus]] */
object GameStatus {
  /** Create the initial game status */
  def apply(parms: GameParameters): GameStatus = {
    val t = Topology(parms.stationName)
    val states = t.blocks.
      map(
        b => (b.id -> initialStatus(b))).
        toMap

    val atrain = MovingTrain("Test", 10, createRoute, 0f, 140f / 3.6f)

    GameStatus(parms, 0f, StationStatus(t, states), new Random(), Set(atrain))
  }

  private def createRoute = {
    val entry = new Vector2f(-SegmentLength * 17.5f, 0)
    val a = entry.add(new Vector2f(SegmentLength * 11, 0))
    val b = a.add(new Vector2f(SegmentLength, TrackGap))
    val c = b.add(new Vector2f(SegmentLength * 11, 0))
    val center1 = a.add(new Vector2f(0f, CurveRadius))
    val center2 = b.add(new Vector2f(0f, -CurveRadius))

    val track1 = SegmentTrack(entry, a)
    val track2 = LeftCurveTrack(center1, CurveRadius, StraightAngle, CurveLength / 2)
    val track3 = RightCurveTrack(center2, CurveRadius, -CurveAngle / 2, CurveLength / 2)
    val track4 = PlatformTrack(b, c)

    TrainRoute(IndexedSeq(track1, track2, track3, track4))
  }

  /** Returns the initial state of Block */
  private def initialStatus(block: Block): BlockStatus = block.template match {
    case Entry => EntryStatus(block)
    case Exit => BlockStatus.exit(block)
    case Platform => BlockStatus.platform(block)
    case Segment => BlockStatus.segment(block)
    case LeftHandSwitch => BlockStatus.switch(block)
    case RightHandSwitch => BlockStatus.switch(block)
  }
}
