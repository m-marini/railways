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
import org.mmarini.scala.jmonkey.MousePrimaryClickedObservable
import java.util.ResourceBundle

/**
 * @author us00852
 *
 */
class EndGameController extends ScreenControllerAdapter
    with ScreenObservables
    with ButtonClickedObservable
    with MousePrimaryClickedObservable
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

  val bundle = Main.bundle

  /** Shows the performance result */
  def show(performance: GamePerformance) {

    val incomeTrainFreq = if (performance.elapsedTime != 0) 3600 * performance.arrivals / performance.elapsedTime else 0
    val outcomeTrainFreq = if (performance.elapsedTime != 0) 3600 * performance.departures / performance.elapsedTime else 0
    val outcomeTrainPerc = if (performance.arrivals > 0) 100 * performance.departures / performance.arrivals else 0
    val errorTrainFreq = if (performance.elapsedTime != 0) 3600 * performance.errors / performance.elapsedTime else 0
    val errorTrainPerc = if (performance.departures > 0) 100 * performance.errors / performance.departures else 0
    val min = (performance.elapsedTime / 60).toInt
    val sec = (performance.elapsedTime - min * 60).toInt

    durationObs.subscribe(_.setText(bundle("durationFormat").format(min, sec)))
    incomeTrainObs.subscribe(rend => rend.setText(bundle("trainCountFormat").format(performance.arrivals)))
    incomeTrainFreqObs.subscribe(rend => rend.setText(bundle("trainFreqFormat").format(incomeTrainFreq)))

    outcomeTrainObs.subscribe(rend => rend.setText(bundle("trainCountFormat").format(performance.departures)))
    outcomeTrainFreqObs.subscribe(rend => rend.setText(bundle("trainFreqFormat").format(outcomeTrainFreq)))
    outcomeTrainPercObs.subscribe(rend => rend.setText(bundle("trainPercFormat").format(outcomeTrainPerc)))

    errorTrainObs.subscribe(rend => rend.setText(bundle("trainCountFormat").format(performance.errors)))
    errorTrainFreqObs.subscribe(rend => rend.setText(bundle("trainFreqFormat").format(errorTrainFreq)))
    errorTrainPercObs.subscribe(rend => rend.setText(bundle("trainPercFormat").format(errorTrainPerc)))
  }
}
