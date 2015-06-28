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
  topology: Topology,
  random: Random,
  blocks: Map[String, BlockStatus],
  trains: Set[Train]) extends LazyLogging {

  /** Creates a new status with a new time value */
  def setTime(time: Float): GameStatus =
    GameStatus(parameters, time, topology, random, blocks, trains)

  /** Creates a new status with time changed by a value */
  def addTime(dt: Float): GameStatus =
    GameStatus(parameters, time + dt, topology, random, blocks, trains)

  /** Creates a new status with a new block map */
  def setBlocks(blocks: Map[String, BlockStatus]): GameStatus =
    GameStatus(parameters, time, topology, random, blocks, trains)

  /** Creates a new status with a new train set */
  def setTrains(trains: Set[Train]): GameStatus =
    GameStatus(parameters, time, topology, random, blocks, trains)

  /** Generates the next status simulating a time elapsing */
  def tick(time: Float): GameStatus = {
    val t = this.time + time
    val newTrains = trains.map(_.tick(time, this))

    //    val nextTrains = trains.map(_.tick(time, this))
    //    val lambda = time * parameters.trainFrequence
    //    val newTrains = generateNewTrains(poisson(lambda))
    //    addTime(t).setTrains(nextTrains ++ newTrains)
    setTrains(newTrains).addTime(t)
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

  /** Generates a random set of trains */
  private def generateNewTrains(n: Int): Set[Train] = {
    (for { i <- 1 to n } yield {
      val id = random.nextInt.toString
      val entry = choice(topology.entries)
      val exit = choice(topology.exits)
      IncomingTrain(id, entry, exit)
    }).toSet
  }

  /** Choices a random element with equal probability distribution */
  private def choice[T](choices: Set[T]): T = {
    val idx = random.nextInt(choices.size)
    choices.toSeq(idx)
  }

  /** Generates the next status changing the status of a block */
  def changeBlockStatus(id: String): GameStatus =
    setBlocks(blocks.
      get(id).
      map(_.changeStatus).
      map(bs => blocks + (id -> bs)).
      getOrElse(blocks))

  /** Generates the next status changing the freedom of a block */
  def changeBlockFreedom(id: String): GameStatus =
    setBlocks(blocks.
      get(id).
      map(_.changeFreedom).
      map(bs => blocks + (id -> bs)).
      getOrElse(blocks))

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

    val atrain = MovingTrain("Test", createRoute, SegmentLength * 1, 140f / 3.6f)

    GameStatus(parms, 0f, t, new Random(), states, Set(atrain))
  }

  private def createRoute = {
    val entry = new Vector2f(-SegmentLength * 17.5f, 0)
    val a = entry.add(new Vector2f(SegmentLength * 11, 0))
    val b = a.add(new Vector2f(SegmentLength, TrackGap))
    val c = b.add(new Vector2f(SegmentLength * 11, 0))
    val center1 = a.add(new Vector2f(0f, CurveRadius))
    val center2 = b.add(new Vector2f(0f, -CurveRadius))

    val track1 = LinearTrack(entry, a)
    val track2 = LeftCurveTrack(center1, CurveRadius, StraightAngle, CurveLength / 2)
    val track3 = RightCurveTrack(center2, CurveRadius, -CurveAngle / 2, CurveLength / 2)
    val track4 = LinearTrack(b, c)

    TrainRoute(IndexedSeq(track1, track2, track3, track4))
  }

  /** Returns the initial state of Block */
  private def initialStatus(block: Block): BlockStatus = block.template match {
    case Entry => EntryStatus(block)
    case Exit => BlockStatus.exit(block)
    case Platform => BlockStatus.platform(block)
    case TrackTemplate => BlockStatus.platform(block)
    case LeftHandSwitch => BlockStatus.switch(block)
    case RightHandSwitch => BlockStatus.switch(block)
  }
}
