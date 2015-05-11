/**
 *
 */
package org.mmarini.railways3d

import com.jme3.app.state.AbstractAppState
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import org.mmarini.railways3d.model.GameHandler
import de.lessvoid.nifty.elements.render.TextRenderer
import org.mmarini.railways3d.model.GameParameters
import scala.collection.parallel.mutable.ParMap
import org.mmarini.railways3d.model.GameParameters
import de.lessvoid.nifty.controls.ButtonClickedEvent
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Observable
import rx.lang.scala.Subject
import rx.lang.scala.Observer
import org.mmarini.railways3d.model.GameParameters

/**
 * @author us00852
 *
 */
class StartController extends AbstractAppState with AbstractController with LazyLogging {

  val selection = Subject[String]()

  val gameParameter = Observer((parms: GameParameters) => parms match {
    case GameParameters(stationName, levelName, durationName, _, _, _, _, _) =>
      station.setText(stationName)
      level.setText(levelName)
      duration.setText(durationName)
  })

  /**
   *
   */
  private def station = screen.findElementByName("station").getRenderer(classOf[TextRenderer])

  /**
   *
   */
  private def level = screen.findElementByName("level").getRenderer(classOf[TextRenderer])

  /**
   *
   */
  private def duration = screen.
    findElementByName("duration").getRenderer(classOf[TextRenderer])

  /**
   *
   */
  def select(id: String): Unit =
    selection.onNext(id)
}
