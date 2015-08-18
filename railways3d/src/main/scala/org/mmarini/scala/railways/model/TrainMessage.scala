package org.mmarini.scala.railways.model

/**
 * @author us00852
 */
trait TrainMessage {
  /** Returns the train id */
  def trainId: String
}

case class TrainEnteredMsg(trainId: String) extends TrainMessage

case class TrainExitedMsg(trainId: String) extends TrainMessage

case class TrainStartedMsg(trainId: String) extends TrainMessage

case class TrainStoppedMsg(trainId: String) extends TrainMessage

case class TrainReloadedMsg(trainId: String) extends TrainMessage

case class TrainWaitForTrackMsg(trainId: String) extends TrainMessage

case class TrainWaitForReloadMsg(trainId: String) extends TrainMessage