/**
 *
 */
package org.mmarini.scala.railways.model

import scala.annotation.migration
import scala.math.exp
import scala.math.min
import scala.util.Random

import org.mmarini.scala.railways.model.blocks.Block
import org.mmarini.scala.railways.model.blocks.BlockStatus
import org.mmarini.scala.railways.model.blocks.EntryBlock
import org.mmarini.scala.railways.model.blocks.EntryStatus
import org.mmarini.scala.railways.model.blocks.ExitBlock
import org.mmarini.scala.railways.model.blocks.ExitStatus
import org.mmarini.scala.railways.model.blocks.LeftHandSwitchBlock
import org.mmarini.scala.railways.model.blocks.PlatformBlock
import org.mmarini.scala.railways.model.blocks.PlatformStatus
import org.mmarini.scala.railways.model.blocks.RightHandSwitchBlock
import org.mmarini.scala.railways.model.blocks.SegmentBlock
import org.mmarini.scala.railways.model.blocks.SegmentStatus
import org.mmarini.scala.railways.model.blocks.SwitchStatus
import org.mmarini.scala.railways.shuffle

import com.typesafe.scalalogging.LazyLogging

/**
 * A set of game parameter, station [[Topology]], elapsed time and set of named [[BlockStatus]]
 *
 * Generates next status by handling the incoming events
 */
case class GameStatus(
    private val parameters: GameParameters,
    val stationStatus: StationStatus,
    private val random: Random,
    trains: Set[Train] = Set(),
    performance: GamePerformance,
    completed: Boolean = false,
    messages: Seq[TrainMessage] = Seq()) extends LazyLogging {

  val LowestTrainId = 100
  val TrainIdCount = 900

  def quit: GameStatus = GameStatus(
    parameters = parameters,
    stationStatus = stationStatus,
    random = random,
    trains = trains,
    performance = performance,
    completed = true,
    messages = messages)

  def isFinished: Boolean = completed || performance.elapsedTime >= parameters.duration

  /** Returns the veicles of the game */
  def vehicles: Set[Vehicle] =
    for {
      train <- trains
      vehicle <- train.vehicles
    } yield vehicle

  /** Creates a new status with a new station status */
  private def setStationStatus(stationStatus: StationStatus): GameStatus =
    GameStatus(
      parameters = parameters,
      stationStatus = stationStatus,
      random = random,
      trains = trains,
      performance = performance,
      completed = completed,
      messages = messages)

  /** Creates a new status with a new train set */
  private def setTrains(trains: Set[Train]): GameStatus =
    GameStatus(
      parameters = parameters,
      stationStatus = stationStatus,
      random = random,
      trains = trains,
      performance = performance,
      completed = completed,
      messages = messages)

  /** Creates a new status with a new train set */
  private def setPerformance(performance: GamePerformance): GameStatus =
    GameStatus(
      parameters = parameters,
      stationStatus = stationStatus,
      random = random,
      trains = trains,
      performance = performance,
      completed = completed,
      messages = messages)

  /** Clears all messages */
  private def clearMessages: GameStatus =
    if (messages.isEmpty) {
      this
    } else {
      new GameStatus(parameters = parameters,
        stationStatus = stationStatus,
        random = random,
        trains = trains,
        performance = performance,
        completed = completed)
    }

  /** Add a message */
  private def add(msg: TrainMessage): GameStatus =
    new GameStatus(parameters = parameters,
      stationStatus = stationStatus,
      random = random,
      trains = trains,
      performance = performance,
      completed = completed,
      messages = messages :+ msg)

  /** Add a sequence of messages */
  private def add(msgs: Seq[TrainMessage]): GameStatus =
    if (msgs.isEmpty) {
      this
    } else {
      new GameStatus(parameters = parameters,
        stationStatus = stationStatus,
        random = random,
        trains = trains,
        performance = performance,
        completed = completed,
        messages = messages ++ msgs)
    }
  /** Generates the next status simulating a time elapsing */
  def tick(time: Float): GameStatus = {
    // Generates status changes for each train e returns the final status
    val trainTickStatus = trains.foldLeft(clearMessages)((status, train) =>
      // Processes single train
      train.tick(time, status) match {
        case (Some(train), msgs) => status.putTrain(train) add msgs
        case (None, msgs) => {
          val errorCountOpt = for {
            (track, _) <- train.trackTailLocation
            (block, _) <- status.stationStatus.extractJunctions(track)
          } yield if (block.id != train.exitId) 1 else 0
          val newPerf = status.performance.
            addErrors(errorCountOpt.getOrElse(0)).
            addDepartures(1)
          val s = status.removeTrain(train).setPerformance(newPerf) add msgs
          s
        }
      })

    val entries = shuffle(trainTickStatus.stationStatus.entryBlocks.filter(_.trainId.isEmpty))(random)
    //   TODO     val n = min(poisson(time * parameters.trainFrequence), entries.size)
    val n = min(poisson(time), entries.size)

    if (n == 0) {
      trainTickStatus.setPerformance(trainTickStatus.performance.addElapsedTime(time))
    } else {
      val exitIds = for (e <- trainTickStatus.stationStatus.exitBlocks) yield e.id
      val newTrainStatus = (0 until n).foldLeft(trainTickStatus)(
        (status, i) => {
          val train = MovingTrain(
            id = createTrainId,
            size = random.nextInt(MaxTrainSize - MinTrainSize) + MinTrainSize,
            entry = entries(i),
            exitId = chooseOneOf(exitIds).get,
            status.performance.elapsedTime)
          status.putTrain(train) add TrainEnteredMsg(train.id)
        })
      newTrainStatus.setPerformance(newTrainStatus.performance.
        addArrivals(n).
        addElapsedTime(time))
    }
  }

  /** Creates a train id */
  private def createTrainId: String =
    s"Exp-${random.nextInt(TrainIdCount) + LowestTrainId}"

  /** Chooses randomly an item */
  private def chooseOneOf[T](seq: IndexedSeq[T]): Option[T] =
    if (seq.isEmpty) {
      None
    } else {
      val n = seq.size
      val idx = random.nextInt(n)
      Some(seq(idx))
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
  private def choice[T](choices: Set[T]): T =
    choices.toSeq(random.nextInt(choices.size))

  /** Generates the next status unlocking a junction*/
  def toogleBlockStatus(id: String)(handler: Int): GameStatus =
    setStationStatus(stationStatus.toogleStatus(id)(handler))

  /** Generates the next status locking a junction*/
  def lockJunction(id: String)(junction: Int): GameStatus =
    setStationStatus(stationStatus.lock(id)(junction))

  /** Generates the next status changing the freedom of a block */
  def unlockJunction(id: String)(junction: Int): GameStatus =
    setStationStatus(stationStatus.unlock(id)(junction))

  /** Generates the next status locking a track */
  def lockTrack(id: String)(junction: Int): GameStatus =
    setStationStatus(stationStatus.lockTrack(id)(junction))

  /** Generates the next status unlocking a track */
  def unlockTrack(id: String)(junction: Int): GameStatus =
    setStationStatus(stationStatus.unlockTrack(id)(junction))

  /** Creates a new status toogling the statuos of a given train */
  def startTrain(id: String): GameStatus = {
    val newStatus = for { train <- trains.find(_.id == id) } yield putTrain(train.start)
    newStatus.getOrElse(this)
  }

  /** Creates a new status toogling the statuos of a given train */
  def stopTrain(id: String): GameStatus = {
    val newStatus = for { train <- trains.find(_.id == id) } yield putTrain(train.stop)
    newStatus.getOrElse(this)
  }

  /** Creates new status reversing a given train */
  def reverseTrain(id: String): GameStatus = {
    logger.debug("Reversing train {}", id)
    val x = for {
      train <- trains.find(_.id == id)
      revTrain <- train.reverse(stationStatus.createReverseRoute)
    } yield putTrain(revTrain)
    x.getOrElse(this)
  }

  /** Locate the block of train head location */
  def trainHeadBlock(train: Train): Option[BlockStatus] =
    for {
      (track, _) <- train.trackLocation(0)
      block <- stationStatus.blocks.values.find(_.contains(track))
    } yield block
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

    GameStatus(
      parameters = parms,
      stationStatus = StationStatus(t, states),
      random = new Random(),
      performance = GamePerformance())
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
