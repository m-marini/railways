/**
 *
 */
package org.mmarini.scala.railways.model

import org.mmarini.scala.railways.model.tracks.Track
import org.mmarini.scala.railways.model.blocks.Block
import org.mmarini.scala.railways.model.blocks.BlockStatus

/**
 * @author us00852
 *
 */
case class StationStatus(topology: Topology, blocks: Map[String, BlockStatus]) {

  /** Generates the next status changing the status of a block */
  def changeBlockStatus(id: String): StationStatus =
    setBlocks(blocks.
      get(id).
      map(_.changeStatus).
      map(bs => blocks + (id -> bs)).
      getOrElse(blocks))

  /** Generates the next status changing the freedom of a block */
  def changeBlockFreedom(id: String): StationStatus =
    setBlocks(blocks.
      get(id).
      map(_.changeFreedom).
      map(bs => blocks + (id -> bs)).
      getOrElse(blocks))

  /** Creates a new status with a new block map */
  def setBlocks(blocks: Map[String, BlockStatus]): StationStatus =
    StationStatus(topology, blocks)

  /** Finds the sequence of tracks available ot allocated by a train starting from a junction of a block */
  def findRoute(block: BlockStatus, junction: Int, trainId: String): IndexedSeq[Track] = {

    def findRoute(tracks: IndexedSeq[Track], block: BlockStatus, junction: Int): IndexedSeq[Track] = {
      // Finds end junctions
      val newTracks = tracks ++ block.tracksForJunction(junction)
      block.junctionFrom(junction) match {
        case None => newTracks
        case Some(end) =>
          findConnection(block)(end) match {
            case None => newTracks
            case Some((nextBlock, nextJunction)) =>
              val trainIdOpt = transitTrain(nextBlock)(nextJunction)
              if (trainIdOpt.isEmpty || trainIdOpt.contains(trainId)) {
                findRoute(newTracks, nextBlock, nextJunction)
              } else {
                newTracks
              }
          }
      }
    }

    findRoute(IndexedSeq(), block, junction)
  }

  /** Returns the train allocating a junction */
  def transitTrain: BlockStatus => Int => Option[String] = (blockStatus) =>
    blockStatus.transitTrain

  /** Finds connection to next block */
  def findConnection: BlockStatus => Int => Option[(BlockStatus, Int)] = (blockStatus) => (junction) =>
    topology.
      findConnection(Endpoint(blockStatus.block, junction)).
      map(ep => (blocks(ep.block.id), ep.index))

  /**
   * Finds the path for a given train
   * The train may not be in this station status
   * To find the path of train:
   * 1. finds the track and location of train tail
   * 2. finds the start junction of block containing the train tail
   * 3. from that finds the sequence of blocks and junctions allocated by this train or available
   *    in this phase the track list may be gathered
   * 4. maps the sequence of blocks to sequence of tracks
   * 5. drop all track before the tail location
   * 6. compute the new location relative to the starting track
   *
   */
  def findRoute(train: Train): (TrainRoute, Float) = {

    val Some((tailTrack, loc)) = train.trackTailLocation
    // Finds block and junction containing the track of train tail
    val Some(startingBlock) = findBlock(tailTrack)
    val (start, _) = startingBlock.junctionsForTrack(tailTrack)

    // map to track list
    val trackList = start.map(
      x => findRoute(startingBlock, x, train.id)).
      toIndexedSeq.flatten
    // drop head until track of tail train
    (TrainRoute(trackList.dropWhile { _ != tailTrack }), loc + train.length)
  }

  /** Returns the block status containing the given track */
  private def findBlock(track: Track): Option[BlockStatus] =
    blocks.values.find(!_.junctionsForTrack(track)._1.isEmpty)

  /** Creates the status of station from current status applying the set of busy junctions */
  def apply(trains: Set[Train]): (StationStatus, Set[Train]) = {
    // Extracts the junctions occupied by trains
    //    val trainTracks: Map[Track, Train] = tempTrainSet.
    val junctionsTrain = for {
      train <- trains
      track <- train.transitTracks
      blockJunction <- extractJunction(track)
    } yield (blockJunction -> train.id)

    val newStationStatus = this(junctionsTrain)
    val newTrains = newStationStatus.computeRoutes(trains)
    (newStationStatus, newTrains)
  }

  /** Computes the new routes for all new train state */
  def computeRoutes(trains: Set[Train]): Set[Train] =
    for { train <- trains } yield {
      val (newRoute, newLocation) = findRoute(train)
      train(newRoute, newLocation)
    }

  /** Creates the status of station from current status applying the set of busy junctions */
  def apply(junctions: Set[((BlockStatus, Int), String)]): StationStatus = {
    val resetBlocks = blocks.values.map(_.noTrainStatus).map(x => (x.id -> x)).toMap

    val newBlocks = junctions.map {
      case ((block, junction), trainId) => resetBlocks(block.id)(junction, Some(trainId))
    }
    val other = resetBlocks.values.filter(x => !newBlocks.exists(_.id == x.id))
    StationStatus(topology, newBlocks ++ other)
  }

  /** Extracts all busy tracks by trainId given the association of allocated track and trainId */
  def extractBusyTrack(trackTrains: Set[(Track, String)]): Set[(Track, String)] =
    for {
      (track, train) <- trackTrains
      blockStatus <- blocks.values
      groupTrack <- blockStatus.trackGroupFor(track)
    } yield {
      (groupTrack, train)
    }

  /** Extracts the blocks and junction of a track */
  def extractJunction(track: Track): Option[(BlockStatus, Int)] = {
    val block = blocks.values.find(x => x.junctionsForTrack(track) match {
      case (Some(junction), _) => true
      case _ => false
    })
    block.map(x => (x, x.junctionsForTrack(track)._1.get))
  }
}

/** Factory of [[StationStatus]] */
object StationStatus {

  /** Creates a StationStatus */
  def apply(topology: Topology, blocks: Set[BlockStatus]): StationStatus =
    StationStatus(topology, blocks.map(x => (x.block.id -> x)).toMap)

}
