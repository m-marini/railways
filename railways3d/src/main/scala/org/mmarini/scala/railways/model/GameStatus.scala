/**
 *
 */
package org.mmarini.scala.railways.model

import com.typesafe.scalalogging.LazyLogging
import scala.util.Random
import scala.math.E
import scala.math.exp
import scala.reflect.api.Position

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

  /** */
  def setTime(time: Float): GameStatus =
    GameStatus(parameters, time, topology, random, blocks, trains)

  /** */
  def addTime(dt: Float): GameStatus =
    GameStatus(parameters, time + dt, topology, random, blocks, trains)

  /** */
  def setBlocks(blocks: Map[String, BlockStatus]): GameStatus =
    GameStatus(parameters, time, topology, random, blocks, trains)

  /** */
  def setTrains(trains: Set[Train]): GameStatus =
    GameStatus(parameters, time, topology, random, blocks, trains)

  /** Generates the next status simulating a time elapsing */
  def tick(time: Float): GameStatus = {
    val t = this.time + time
    val nextTrains = trains.map(_.tick(time, this))
    val lambda = time * parameters.trainFrequence
    val newTrains = generateNewTrains(poisson(lambda))
    addTime(t).setTrains(nextTrains ++ newTrains)
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
    GameStatus(parms, 0f, t, new Random(), states, Set.empty)
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
