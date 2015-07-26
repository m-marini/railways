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
import org.mmarini.scala.railways.model.blocks.EntryStatus

/**
 * A set of game parameter, station [[Topology]], elapsed time and set of named [[BlockStatus]]
 *
 * Generates next status by handling the incoming events
 */
case class GameStatus(
    private val parameters: GameParameters,
    val time: Float = 0,
    val stationStatus: StationStatus,
    private val random: Random,
    trains: Set[Train] = Set()) extends LazyLogging {

  /** Returns the veicles of the game */
  def vehicles: Set[Vehicle] =
    for {
      train <- trains
      vehicle <- train.vehicles
    } yield vehicle

  /** Creates a new status with a new time value */
  private def setTime(time: Float): GameStatus =
    GameStatus(parameters, time, stationStatus, random, trains)

  /** Creates a new status with time changed by a value */
  private def addTime(dt: Float): GameStatus =
    GameStatus(parameters, time + dt, stationStatus, random, trains)

  /** Creates a new status with a new station status */
  private def setStationStatus(stationStatus: StationStatus): GameStatus =
    GameStatus(parameters, time, stationStatus, random, trains)

  /** Creates a new status with a new train set */
  private def setTrains(trains: Set[Train]): GameStatus =
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
  private def removeTrain(train: Train) =
    nextStatusForTrains(trains.filterNot(_.id == train.id))

  /** Puts a new train status */
  private def putTrain(train: Train) =
    nextStatusForTrains(trains.filterNot(_.id == train.id) + train)

  /**
   * Create the next status of game for a given set of train
   * Computes the new station status by setting transit trains property and
   * computes the new route for each train
   */
  private def nextStatusForTrains(trains: Set[Train]) = {
    val (newStationStatus, trainSet) = stationStatus.apply(trains)
    setTrains(trainSet).setStationStatus(newStationStatus)
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
    val states = (for {
      b <- t.blocks
    } yield (b.id -> initialStatus(b))).
      toMap

    //    val atrain = MovingTrain("Test", 10, createRoute, 0f, 0f / 3.6f)
    val initStatus = GameStatus(parms, stationStatus = StationStatus(t, states), random = new Random())
    val trainOpt = for {
      bs <- initStatus.stationStatus.blocks.get("entry")
    } yield MovingTrain(id = "Test", 11, bs.asInstanceOf[EntryStatus])

    val ngs = for (train <- trainOpt) yield initStatus.putTrain(train)
    ngs.getOrElse(initStatus)
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
