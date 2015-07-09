/**
 *
 */
package org.mmarini.scala.railways.model

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

  /**
   * Finds the path for a given train
   * The train may not be in this station status
   */
  def findRoute(train: Train): (TrainRoute, Float) = {
    val Some((track, loc)) = train.trackTailLocation
    // Finds block and junction containing the track of train tail
    val (block, startJunction): (BlockStatus, Int) = ??? // blocks.find ... !block.junction.isEmpty

    // Finds track sequence starting at block, startJunction for train (track allocated by the train) 
    val trackList: IndexedSeq[Track] = ???

    ???
  }

  /** Creates the status of station from current status applying the set of busy tracks */
  def apply(busyTracks: Set[Track]): StationStatus =
    StationStatus(
      topology,
      blocks.map {
        case (id, block) => (id, block(busyTracks))
      })

  /** Extracts all busy tracks by train given the association of allocated track and train */
  def extractBusyTrack(trainTracks: Set[(Track, Train)]): Set[(Track, Train)] =
    for {
      (track, train) <- trainTracks
      blockStatus <- blocks.values
      groupTrack <- blockStatus.trackGroupFor(track)
    } yield {
      (groupTrack, train)
    }
}
