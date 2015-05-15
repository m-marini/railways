/**
 *
 */
package org.mmarini.railways3d

import com.jme3.app.state.AbstractAppState
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.controls.DropDown
import de.lessvoid.nifty.controls.CheckBox
import de.lessvoid.nifty.controls.Slider
import org.mmarini.railways3d.model.GameParameters
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Observable
import rx.lang.scala.Subscription
import rx.lang.scala.Subject
import org.mmarini.railways3d.model.GameParameters

/**
 * @author us00852
 *
 */
class OptionsController extends AbstractAppState with AbstractController with LazyLogging {

  val FrequenceEnum = new Enumeration {
    val Easy, Medium, Difficult, Custom = Value
    val valueById = Map(Easy -> 10, Medium -> 20, Difficult -> 60).map { case (k, v) => (k.id -> v.toFloat / 60) }
  }

  val DurationEnum = new Enumeration {
    val Short, Medium, Long, Custom = Value
    val valueById = Map(Short -> 5, Medium -> 10, Long -> 30).map { case (k, v) => (k.id -> v.toFloat * 60) }
  }

  private def station = screen.findNiftyControl("station", classOf[DropDown[String]])

  private def level = screen.findNiftyControl("level", classOf[DropDown[String]])

  private def duration = screen.findNiftyControl("duration", classOf[DropDown[String]])

  private def autoLock = screen.findNiftyControl("autoLock", classOf[CheckBox])

  private def mute = screen.findNiftyControl("mute", classOf[CheckBox])

  private def volume = screen.findNiftyControl("volume", classOf[Slider])

  private val _gameParameters = Subject[GameParameters]()

  private val _completed = Subject[String]()

  /**
   *
   */
  def gameParameters: Observable[GameParameters] = _gameParameters

  /**
   *
   */
  def completed: Observable[String] = _completed

  /**
   *
   */
  override def bind(nifty: Nifty, screen: Screen) {
    super.bind(nifty, screen)

    for (s <- List("Delta Crossing", "Downville Station", "Jackville Terminal", "Passing Station"))
      station.addItem(s)
    station.selectItemByIndex(0)

    for (s <- List("Facile", "Medio", "Difficile", "Personalizzato"))
      level.addItem(s)
    level.selectItemByIndex(0)

    for (s <- List("Corto (5 min.)", "Medio (15 min.)", "Lungo (30 min.)", "Personalizzato"))
      duration.addItem(s)
    duration.selectItemByIndex(0)
  }

  /**
   *
   */
  def okPressed {
    _gameParameters.onNext(
      GameParameters(
        station.getSelection,
        level.getSelection,
        duration.getSelection,
        FrequenceEnum.valueById(level.getSelectedIndex),
        DurationEnum.valueById(duration.getSelectedIndex),
        autoLock.isChecked,
        mute.isChecked,
        volume.getValue / 100))
    _completed.onNext("completed")
  }
}
