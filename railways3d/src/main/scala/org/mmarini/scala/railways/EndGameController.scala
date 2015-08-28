/**
 *
 */
package org.mmarini.scala.railways

import rx.lang.scala.Observable
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.controls._
import org.mmarini.scala.jmonkey.ButtonClickedObservable
import org.mmarini.scala.railways.model.GamePerformance
import de.lessvoid.nifty.elements.render.TextRenderer
import org.mmarini.scala.railways.model.GamePerformance
import org.mmarini.scala.jmonkey.ScreenControllerAdapter
import org.mmarini.scala.jmonkey.ScreenObservables
import rx.lang.scala.Subscription
import rx.lang.scala.subscriptions.CompositeSubscription

/**
 * @author us00852
 *
 */
class EndGameController extends ScreenControllerAdapter
    with ScreenObservables
    with ButtonClickedObservable
    with LazyLogging {

  private def durationObs = redererByIdObs("duration", classOf[TextRenderer])

  private def errorTrainObs = redererByIdObs("error-train", classOf[TextRenderer])

  private def errorTrainFreqObs = redererByIdObs("error-train-freq", classOf[TextRenderer])

  private def errorTrainPercObs = redererByIdObs("error-train-perc", classOf[TextRenderer])

  private def incomeTrainObs = redererByIdObs("income-train", classOf[TextRenderer])

  private def incomeTrainFreqObs = redererByIdObs("income-train-freq", classOf[TextRenderer])

  private def outcomeTrainObs = redererByIdObs("outcome-train", classOf[TextRenderer])

  private def outcomeTrainFreqObs = redererByIdObs("outcome-train-freq", classOf[TextRenderer])

  private def outcomeTrainPercObs = redererByIdObs("outcome-train-perc", classOf[TextRenderer])

  /** Shows the performance result */
  def show(performance: GamePerformance) {
    durationObs.subscribe(_.setText(f"${(performance.elapsedTime / 60).toInt}%d '"))

    errorTrainObs.subscribe(_.setText(f"${performance.errors}%d trains"))
    errorTrainFreqObs.subscribe(_.setText(f"${performance.errors / performance.elapsedTime * 3600}%g trains/h"))
    errorTrainPercObs.subscribe(_.setText(
      if (performance.departures > 0) {
        f"${performance.errors * 100 / performance.departures}%d %%"
      } else {
        "0 %"
      }))

    outcomeTrainObs.subscribe(_.setText(f"${performance.departures}%d trains"))
    outcomeTrainFreqObs.subscribe(_.setText(f"${performance.departures / performance.elapsedTime * 3600}%g trains/h"))
    outcomeTrainPercObs.subscribe(_.setText(
      if (performance.arrivals > 0) {
        f"${performance.departures * 100 / performance.arrivals}%d %%"
      } else {
        "0 %"
      }))

    incomeTrainObs.subscribe(_.setText(f"${performance.arrivals}%d %%"))
    incomeTrainFreqObs.subscribe(_.setText(f"${performance.arrivals / performance.elapsedTime * 3600}%g trains/h"))
  }
}
