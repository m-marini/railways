/**
 *
 */
package org.mmarini.scala.railways.model

import scala.IndexedSeq
import scala.math.exp
import scala.util.Random
import com.jme3.math.Vector2f
import com.typesafe.scalalogging.LazyLogging
import org.mmarini.scala.railways.model.tracks.SegmentTrack
import org.mmarini.scala.railways.model.tracks.RightCurveTrack
import org.mmarini.scala.railways.model.tracks.PlatformTrack
import org.mmarini.scala.railways.model.tracks.LeftCurveTrack
import org.mmarini.scala.railways.model.blocks.ExitBlock
import org.mmarini.scala.railways.model.blocks.LeftHandSwitchBlock
import org.mmarini.scala.railways.model.blocks.RightHandSwitchBlock
import org.mmarini.scala.railways.model.blocks.SegmentBlock
import org.mmarini.scala.railways.model.blocks.Block
import org.mmarini.scala.railways.model.blocks.EntryBlock
import org.mmarini.scala.railways.model.blocks.PlatformBlock
import org.mmarini.scala.railways.model.blocks.EntryStatus
import org.mmarini.scala.railways.model.blocks.BlockStatus
import org.mmarini.scala.railways.model.tracks.HiddenTrack
import org.mmarini.scala.railways.model.blocks.EntryStatus
import org.mmarini.scala.railways.model.blocks.SegmentStatus
import org.mmarini.scala.railways.model.blocks.PlatformStatus
import org.mmarini.scala.railways.model.blocks.SwitchStatus
import org.mmarini.scala.railways.model.blocks.ExitStatus

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
    val newStatus = trains.foldLeft(this)((status, train) =>
      // Processes single train
      train.tick(time, status) match {
        case Some(train) => status.putTrain(train)
        case _ => status.removeTrain(train)
      })

    // TODO generare nuovi treni 
    val newTrainStatus = newStatus

    newTrainStatus.addTime(time)
  }

  /** Removes a train from the train list */
  def removeTrain(train: Train): GameStatus = setTrains(trains.filterNot(_.id == train.id))

  /**
   * Puts a new train status.
   * Creates the list of train without the new train
   * Adds to it the new train
   * Creates the set of associations between the tracks occupied by each train
   * Extends the set of associations to the tracks of groups occupied by each train
   * Generates the new status of station applying all the busy tracks
   * Generates the new status of trains computing the new route for each train
   */
  private def putTrain(train: Train): GameStatus = {
    val otherTrains = trains.filterNot(_.id == train.id)
    val tempTrainSet = otherTrains + train

    // Extracts the track occupied by trains
    //    val trainTracks: Map[Track, Train] = tempTrainSet.
    val trainTracks = for {
      train <- tempTrainSet
      track <- train.transitTracks
    } yield (track -> train.id)

    // Extract the busy track (by mutex block tracks) 
    val busyTracks = stationStatus.extractBusyTrack(trainTracks)

    // Update the station status with new busyTrack
    val newStationStatus = stationStatus.apply(busyTracks)

    // Computes the new routes for all new train state
    val newTrainSet = for { train <- tempTrainSet } yield {
      val (newRoute, newLocation) = newStationStatus.findRoute(train)
      train(newRoute, newLocation)
    }

    setTrains(tempTrainSet).setStationStatus(newStationStatus)
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

    val atrain = MovingTrain("Test", 10, createRoute, 0f, 0f / 3.6f)

    GameStatus(parms, 0f, StationStatus(t, states), new Random(), Set(atrain))
  }

  private def createRoute = {
    val entry = new Vector2f(-SegmentLength * 17.5f, 0)
    val a = entry.add(new Vector2f(SegmentLength * 11, 0))
    val b = a.add(new Vector2f(SegmentLength, TrackGap))
    val c = b.add(new Vector2f(SegmentLength * 11, 0))
    val center1 = a.add(new Vector2f(0f, CurveRadius))
    val center2 = b.add(new Vector2f(0f, -CurveRadius))

    val track0 = HiddenTrack
    val track1 = SegmentTrack(entry, a)
    val track2 = LeftCurveTrack(center1, CurveRadius, StraightAngle, CurveLength / 2)
    val track3 = RightCurveTrack(center2, CurveRadius, -CurveAngle / 2, CurveLength / 2)
    val track4 = PlatformTrack(b, c)

    TrainRoute(IndexedSeq(track1, track2, track3, track4))
  }

  /** Returns the initial state of Block */
  private def initialStatus(block: Block): BlockStatus = block match {
    case x: EntryBlock => EntryStatus(x)
    case x: ExitBlock => ExitStatus(x)
    case x: PlatformBlock => PlatformStatus(x)
    case x: SegmentBlock => SegmentStatus(x)
    case x: LeftHandSwitchBlock => SwitchStatus(x)
    case x: RightHandSwitchBlock => SwitchStatus(x)
  }
}
