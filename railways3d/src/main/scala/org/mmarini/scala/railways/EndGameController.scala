/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.DefaultScreenController
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.controls._
import org.mmarini.scala.jmonkey.ButtonClickedObservable
import org.mmarini.scala.railways.model.GamePerformance
import de.lessvoid.nifty.elements.render.TextRenderer
import org.mmarini.scala.railways.model.GamePerformance

/**
 * @author us00852
 *
 */
class EndGameController extends AbstractAppState
    with DefaultScreenController
    with ButtonClickedObservable
    with LazyLogging {

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
