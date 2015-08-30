/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.MousePrimaryClickedObservable
import org.mmarini.scala.jmonkey.MousePrimaryReleaseObservable
import org.mmarini.scala.jmonkey.NiftyObservables
import org.mmarini.scala.jmonkey.ScreenControllerAdapter
import org.mmarini.scala.jmonkey.ScreenObservables
import org.mmarini.scala.railways.model.GamePerformance
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.elements.render.TextRenderer
import java.util.ResourceBundle
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

  private lazy val durationObs = redererByIdObs("perf-duration", classOf[TextRenderer])
  private lazy val incomeTrainObs = redererByIdObs("perf-arrivals", classOf[TextRenderer])
  private lazy val incomeTrainFreqObs = redererByIdObs("perf-arrivals-freq", classOf[TextRenderer])

  private lazy val outcomeTrainObs = redererByIdObs("perf-departures", classOf[TextRenderer])
  private lazy val outcomeTrainFreqObs = redererByIdObs("perf-departures-freq", classOf[TextRenderer])
  private lazy val outcomeTrainPercObs = redererByIdObs("perf-departures-perc", classOf[TextRenderer])
  private lazy val errorTrainObs = redererByIdObs("perf-errors", classOf[TextRenderer])
  private lazy val errorTrainFreqObs = redererByIdObs("perf-errors-freq", classOf[TextRenderer])
  private lazy val errorTrainPercObs = redererByIdObs("perf-errors-perc", classOf[TextRenderer])

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
