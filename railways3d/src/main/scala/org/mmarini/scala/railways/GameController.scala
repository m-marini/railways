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
import org.mmarini.scala.jmonkey.ScreenObservables
import org.mmarini.scala.jmonkey.NiftyObservables

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class GameController extends ScreenControllerAdapter
    with NiftyObservables
    with ScreenObservables
    with MousePrimaryClickedObservable
    with MousePrimaryReleaseObservable
    with LazyLogging {

  private val trainPopupObs = createPopupObs("trainPopup").cache(1)
  private val semPopupValue = createPopupObs("semPopup").cache(1)

  trainPopupObs.subscribe()
  semPopupValue.subscribe()

  private def msgCtrlObs = controllerByIdObs("messagesPanel", classOf[MessageController])

  private def trainStatusString(train: Train): String = train match {
    case _: MovingTrain => f"${train.id}%s moving at ${(train.speed * 3.6).toInt}%d Km/h"
    case _: StoppingTrain => f"${train.id}%s brackingt at ${(train.speed * 3.6).toInt}%d Km/h"
    case _: StoppedTrain => f"${train.id}%s stopped"
    case _: WaitForPassengerTrain => f"${train.id}%s waiting for passenger"
    case _: WaitingForTrackTrain => f"${train.id}%s waiting for semaphore"
    case _ => "???"
  }
//
//  def showSemaphorePopup(pos: Vector2f) {
//    semPopupValue.valueObs.subscribe(s => showPopupAt(s, "semPane", pos))
//  }
//
//  def showTrainPopup(pos: Vector2f) {
//    trainPopupValue.valueObs.subscribe(s => showPopupAt(s, "semPane", pos))
//  }

//  private def durationObs = redererObs("perf-duration", classOf[TextRenderer])
//  private def incomeTrainObs = redererObs("perf-arrivals", classOf[TextRenderer])
//  private def incomeTrainFreqObs = redererObs("perf-arrivals-freq", classOf[TextRenderer])
//
//  private def outcomeTrainObs = redererObs("perf-departures", classOf[TextRenderer])
//  private def outcomeTrainFreqObs = redererObs("perf-departures-freq", classOf[TextRenderer])
//  private def outcomeTrainPercObs = redererObs("perf-departures-perc", classOf[TextRenderer])
//  private def errorTrainObs = redererObs("perf-errors", classOf[TextRenderer])
//  private def errorTrainFreqObs = redererObs("perf-errors-freq", classOf[TextRenderer])
//  private def errorTrainPercObs = redererObs("perf-errors-perc", classOf[TextRenderer])
//
//  /** Shows the performance result */
//  def show(performance: GamePerformance) {
//    durationObs.subscribe(rend => rend.setText(f"${(performance.elapsedTime / 60).toInt}%d'"))
//
//    incomeTrainObs.subscribe(rend => rend.setText(f"${performance.arrivals}%d trains"))
//    incomeTrainFreqObs.subscribe(rend => rend.setText(f"${3600 * performance.arrivals / performance.elapsedTime}%.0f trains/h"))
//
//    outcomeTrainObs.subscribe(rend => rend.setText(f"${performance.departures}%d trains"))
//    outcomeTrainFreqObs.subscribe(rend => rend.setText(f"${performance.departures / performance.elapsedTime * 3600}%.0f trains/h"))
//    outcomeTrainPercObs.subscribe(rend => rend.setText(
//      if (performance.arrivals > 0)
//        f"${100 * performance.departures / performance.arrivals}%d %%"
//      else
//        "0 %"))
//
//    errorTrainObs.subscribe(rend => rend.setText(f"${performance.errors}%d trains"))
//    errorTrainFreqObs.subscribe(rend => rend.setText(f"${3600 * performance.errors / performance.elapsedTime}%.0f trains/h"))
//    errorTrainPercObs.subscribe(rend => rend.setText(
//      if (performance.departures > 0)
//        f"${100 * performance.errors / performance.departures}%d %%"
//      else
//        "0 %"))
//  }

}
