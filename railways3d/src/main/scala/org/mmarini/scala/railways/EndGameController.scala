/**
 *
 */
package org.mmarini.scala.railways

import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.controls._
import org.mmarini.scala.jmonkey.ButtonClickedObservable
import org.mmarini.scala.railways.model.GamePerformance
import de.lessvoid.nifty.elements.render.TextRenderer
import org.mmarini.scala.railways.model.GamePerformance
import org.mmarini.scala.jmonkey.ScreenControllerAdapter
import org.mmarini.scala.jmonkey.ScreenAdapter

/**
 * @author us00852
 *
 */
class EndGameController extends ScreenControllerAdapter
    with ScreenAdapter
    with ButtonClickedObservable
    with LazyLogging {

  private def duration = redererById("duration", classOf[TextRenderer])

  private def errorTrain = redererById("error-train", classOf[TextRenderer])

  private def errorTrainFreq = redererById("error-train-freq", classOf[TextRenderer])

  private def errorTrainPerc = redererById("error-train-perc", classOf[TextRenderer])

  private def incomeTrain = redererById("income-train", classOf[TextRenderer])

  private def incomeTrainFreq = redererById("income-train-freq", classOf[TextRenderer])

  private def outcomeTrain = redererById("outcome-train", classOf[TextRenderer])

  private def outcomeTrainFreq = redererById("outcome-train-freq", classOf[TextRenderer])

  private def outcomeTrainPerc = redererById("outcome-train-perc", classOf[TextRenderer])

  /** Shows the performance result */
  def show(performance: GamePerformance) {
    for (rend <- duration)
      rend.setText(f"${(performance.elapsedTime / 60).toInt}%d '")

    for (rend <- errorTrain)
      rend.setText(f"${performance.errors}%d trains")
    for (rend <- errorTrainFreq)
      rend.setText(f"${performance.errors / performance.elapsedTime * 3600}%g trains/h")
    for (rend <- errorTrainPerc)
      rend.setText(
        if (performance.departures > 0)
          f"${performance.errors * 100 / performance.departures}%d %%"
        else
          "0 %")

    for (rend <- outcomeTrain)
      rend.setText(f"${performance.departures}%d trains")
    for (rend <- outcomeTrainFreq)
      rend.setText(f"${performance.departures / performance.elapsedTime * 3600}%g trains/h")
    for (rend <- outcomeTrainPerc)
      rend.setText(
        if (performance.arrivals > 0)
          f"${performance.departures * 100 / performance.arrivals}%d %%"
        else
          "0 %")

    for (rend <- incomeTrain)
      rend.setText(f"${performance.arrivals}%d %%")
    for (rend <- incomeTrainFreq)
      rend.setText(f"${performance.arrivals / performance.elapsedTime * 3600}%g trains/h")
  }
}
