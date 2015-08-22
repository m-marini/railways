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
import org.mmarini.scala.jmonkey.ScreenControllerAdapter
import org.mmarini.scala.jmonkey.MousePrimaryClickedObservable
import org.mmarini.scala.jmonkey.MousePrimaryReleaseObservable
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
import org.mmarini.scala.jmonkey.ScreenAdapter
import org.mmarini.scala.jmonkey.NiftyUtil

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class GameController extends ScreenControllerAdapter
    with ScreenAdapter
    with NiftyUtil
    with MousePrimaryClickedObservable
    with MousePrimaryReleaseObservable
    with ListBoxSelectionChangedObservable[String]
    with LazyLogging {

  lazy val trainPopupOpt = createPopup("trainPopup")

  lazy val semPopupOpt = createPopup("semPopup")

  def cameraCtrlOpt = controllerById("cameraPanel", classOf[CameraController])

  def trainCtrlOpt = controllerById("trainPanel", classOf[TrainController])

  private def msgCtrlOpt = controllerById("messagesPanel", classOf[MessageController])

  /** Shows the messages in the messages list panel */
  def showMsgs(msgs: Seq[TrainMessage]) {
    for { ctrl <- msgCtrlOpt }
      ctrl.show(for { msg <- msgs } yield msg.toString)
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

  private def durationOpt = redererById("perf-duration", classOf[TextRenderer])
  private def incomeTrainOpt = redererById("perf-arrivals", classOf[TextRenderer])
  private def incomeTrainFreqOpt = redererById("perf-arrivals-freq", classOf[TextRenderer])

  private def outcomeTrainOpt = redererById("perf-departures", classOf[TextRenderer])
  private def outcomeTrainFreqOpt = redererById("perf-departures-freq", classOf[TextRenderer])
  private def outcomeTrainPercOpt = redererById("perf-departures-perc", classOf[TextRenderer])
  private def errorTrainOpt = redererById("perf-errors", classOf[TextRenderer])
  private def errorTrainFreqOpt = redererById("perf-errors-freq", classOf[TextRenderer])
  private def errorTrainPercOpt = redererById("perf-errors-perc", classOf[TextRenderer])

  /** Shows the performance result */
  def show(performance: GamePerformance) {
    for (rend <- durationOpt)
      rend.setText(f"${(performance.elapsedTime / 60).toInt}%d'")

    for (rend <- incomeTrainOpt)
      rend.setText(f"${performance.arrivals}%d trains")
    for (rend <- incomeTrainFreqOpt)
      rend.setText(f"${3600 * performance.arrivals / performance.elapsedTime}%.0f trains/h")

    for (rend <- outcomeTrainOpt)
      rend.setText(f"${performance.departures}%d trains")
    for (rend <- outcomeTrainFreqOpt)
      rend.setText(f"${performance.departures / performance.elapsedTime * 3600}%.0f trains/h")
    for (rend <- outcomeTrainPercOpt)
      rend.setText(
        if (performance.arrivals > 0)
          f"${100 * performance.departures / performance.arrivals}%d %%"
        else
          "0 %")

    for (rend <- errorTrainOpt)
      rend.setText(f"${performance.errors}%d trains")
    for (rend <- errorTrainFreqOpt)
      rend.setText(f"${3600 * performance.errors / performance.elapsedTime}%.0f trains/h")
    for (rend <- errorTrainPercOpt)
      rend.setText(
        if (performance.departures > 0)
          f"${100 * performance.errors / performance.departures}%d %%"
        else
          "0 %")
  }

}
