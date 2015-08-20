/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.GameParameters
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.ListBox
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent
import de.lessvoid.nifty.screen.Screen
import rx.lang.scala.Observer
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEventSubscriber
import scala.util.Try
import de.lessvoid.nifty.elements.Element
import com.jme3.math.Vector2f
import org.mmarini.scala.jmonkey.ScreenObservable
import org.mmarini.scala.jmonkey.AbstractScreenController
import org.mmarini.scala.jmonkey.MousePrimarClickedObservable
import org.mmarini.scala.jmonkey.MousePrimarReleaseObservable
import org.mmarini.scala.jmonkey.ListBoxSelectionChangedObservable
import rx.lang.scala.Observable
import org.mmarini.scala.railways.model.TrainMessage
import org.mmarini.scala.railways.model.Train
import org.mmarini.scala.railways.model.MovingTrain
import org.mmarini.scala.railways.model.StoppingTrain
import org.mmarini.scala.railways.model.StoppedTrain
import org.mmarini.scala.railways.model.WaitForPassengerTrain
import org.mmarini.scala.railways.model.WaitingForTrackTrain
import org.mmarini.scala.railways.model.GamePerformance
import de.lessvoid.nifty.elements.render.TextRenderer

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class GameController extends AbstractAppState
    with AbstractScreenController
    with ScreenObservable
    with MousePrimarClickedObservable
    with MousePrimarReleaseObservable
    with ListBoxSelectionChangedObservable[String]
    with LazyLogging {

  /** the observable of camera selection */
  lazy val cameraSelectedObs = singleSelection(listBoxSelectionChangedObs)

  lazy val trainPopupOpt = createPopup("trainPopup")

  lazy val semPopupOpt = createPopup("semPopup")

  private def logListOpt = controlById("messagesList", classOf[ListBox[String]])

  private def trainListOpt = controlById("trainsList", classOf[ListBox[String]])

  private def cameraCtrlOpt = controllerById("cameraPanel", classOf[CameraController])

  private def trainCtrlOpt = controllerById("trainPanel", classOf[TrainController])

  private def msgCtrlOpt = controllerById("messagesPanel", classOf[MessageController])

  /** Shows the camera views in the camera list panel */
  def showCameras(cams: List[String]) {
    for {
      c <- controlById("camerasList", classOf[ListBox[String]])
      item <- cams
    } c.addItem(item)
    for { ctrl <- cameraCtrlOpt } ctrl.showCameras(cams)
  }

  /** Shows the messages in the messages list panel */
  def showMsgs(msgs: Seq[TrainMessage]) {
    for { ctrl <- msgCtrlOpt }
      ctrl.show(for { msg <- msgs } yield msg.toString)
      
    for {
      c <- logListOpt
    } {
      val current = c.getItems.size
      val removing = current + msgs.size - 5;
      if (removing > current)
        c.clear
      else if (removing > 0) {
        for (i <- current - removing until current)
          c.removeItemByIndex(i)
      }
      for {
        item <- msgs
      } c.insertItem(item.toString(), 0)
    }
  }

  /** Shows the trains in the trains list panel */
  def showTrains(trains: Set[Train]) {
    for {
      ctrl <- trainListOpt
    } {
      ctrl.clear
      for {
        train <- trains
      } {
        val item = trainStatusString(train)
        ctrl.addItem(item)
      }
    }
    for {
      ctrl <- trainCtrlOpt
    } ctrl.show(trains.toSeq)
  }

  private def trainStatusString(train: Train): String = train match {
    case _: MovingTrain => f"${train.id}%s moving at ${(train.speed * 3.6).toInt}%d Km/h"
    case _: StoppingTrain => f"${train.id}%s brackingt at ${(train.speed * 3.6).toInt}%d Km/h"
    case _: StoppedTrain => f"${train.id}%s stopped"
    case _: WaitForPassengerTrain => f"${train.id}%s waiting for passenger"
    case _: WaitingForTrackTrain => f"${train.id}%s waiting for semaphore"
    case _ => "???"
  }

  def showSemaphorePopup(pos: Vector2f) {
    for (s <- semPopupOpt)
      showPopupAt(s, "semPane", pos)
  }

  def showTrainPopup(pos: Vector2f) {
    for (s <- trainPopupOpt)
      showPopupAt(s, "trainPane", pos)
  }

  private def duration = redererById("duration", classOf[TextRenderer])

  private def correctTrain = redererById("correct-train", classOf[TextRenderer])

  private def correctTrainFreq = redererById("correct-train-freq", classOf[TextRenderer])

  private def correctTrainPerc = redererById("correct-train-perc", classOf[TextRenderer])

  private def incomeTrain = redererById("income-train", classOf[TextRenderer])

  private def incomeTrainFreq = redererById("income-train-freq", classOf[TextRenderer])

  private def outcomeTrain = redererById("outcome-train", classOf[TextRenderer])

  private def outcomeTrainFreq = redererById("outcome-train-freq", classOf[TextRenderer])

  private def outcomeTrainPerc = redererById("outcome-train-perc", classOf[TextRenderer])

  /** Shows the performance result */
  def show(performance: GamePerformance) {
    for (rend <- duration)
      rend.setText(f"${(performance.elapsedTime / 60).toInt}%d '")

    for (rend <- correctTrain)
      rend.setText(f"${performance.rightRoutedTrainCount}%d trains")
    for (rend <- correctTrainFreq)
      rend.setText(f"${performance.rightRoutedTrainCount / performance.elapsedTime * 3600}%g trains/h")
    for (rend <- correctTrainPerc)
      rend.setText(
        if (performance.exitedTrainCount > 0)
          f"${performance.rightRoutedTrainCount * 100 / performance.exitedTrainCount}%d %%"
        else
          "0 %")

    for (rend <- outcomeTrain)
      rend.setText(f"${performance.exitedTrainCount}%d trains")
    for (rend <- outcomeTrainFreq)
      rend.setText(f"${performance.exitedTrainCount / performance.elapsedTime * 3600}%g trains/h")
    for (rend <- outcomeTrainPerc)
      rend.setText(
        if (performance.enteredTrainCount > 0)
          f"${performance.exitedTrainCount * 100 / performance.enteredTrainCount}%d %%"
        else
          "0 %")

    for (rend <- incomeTrain)
      rend.setText(f"${performance.enteredTrainCount}%d %%")
    for (rend <- incomeTrainFreq)
      rend.setText(f"${performance.enteredTrainCount / performance.elapsedTime * 3600}%g trains/h")
  }

}
