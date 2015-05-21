/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.GameParameters

import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging

import de.lessvoid.nifty.elements.render.TextRenderer
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subject

/**
 * @author us00852
 *
 */
class StartController extends AbstractAppState with AbstractController with LazyLogging {

  private val _selection = Subject[String]()

  /**
   *
   */
  def selection: Observable[String] = _selection

  /**
   *
   */
  val gameParameterObserver = Observer((parms: GameParameters) => parms match {
    case GameParameters(stationName, levelName, durationName, _, _, _, _, _) =>
      station.foreach(_.setText(stationName))
      level.foreach(_.setText(levelName))
      duration.foreach(_.setText(durationName))
  })

  /**
   *
   */
  private def station = screen.map(_.findElementByName("station").getRenderer(classOf[TextRenderer]))

  /**
   *
   */
  private def level = screen.map(_.findElementByName("level").getRenderer(classOf[TextRenderer]))

  /**
   *
   */
  private def duration = screen.map(_.findElementByName("duration").getRenderer(classOf[TextRenderer]))

  /**
   *
   */
  def select(id: String): Unit =
    _selection.onNext(id)
}
