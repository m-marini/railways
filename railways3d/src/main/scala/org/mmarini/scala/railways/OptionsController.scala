/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.GameParameters
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.CheckBox
import de.lessvoid.nifty.controls._
import de.lessvoid.nifty.controls.Slider
import de.lessvoid.nifty.screen.Screen
import rx.lang.scala.Observable
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEventSubscriber
import org.mmarini.scala.jmonkey.SelectionObservable
import org.mmarini.scala.jmonkey.DefaultScreenController

/**
 * @author us00852
 *
 */
class OptionsController extends AbstractAppState
    with DefaultScreenController
    with SelectionObservable
    with LazyLogging {

  val FrequenceEnum = new Enumeration {
    val Easy, Medium, Difficult, Custom = Value
    val valueById = Map(Easy -> 20, Medium -> 50, Difficult -> 100).map { case (k, v) => (k.id -> v.toFloat / 3600f) }
  }

  val DurationEnum = new Enumeration {
    val Short, Medium, Long, Custom = Value
    val valueById = Map(Short -> 1, Medium -> 10, Long -> 30).map { case (k, v) => (k.id -> v.toFloat * 60) }
  }

  private def station = controlById("station", classOf[DropDown[String]])

  private def level = controlById("level", classOf[DropDown[String]])

  private def duration = controlById("duration", classOf[DropDown[String]])

  private def autoLock = controlById("autoLock", classOf[CheckBox])

  private def mute = controlById("mute", classOf[CheckBox])

  private def volume = controlById("volume", classOf[Slider])

  private val _confirmed = Subject[String]()

  private val DefaultParms = GameParameters(
    "Downville Station",
    "Facile",
    "Corto (5 min.)",
    FrequenceEnum.valueById(0),
    DurationEnum.valueById(0),
    true,
    false,
    0.5f)

  /**
   *
   */
  override def bind(nifty: Nifty, screen: Screen) {
    super.bind(nifty, screen)

    for {
      st <- station
      value <- List(
        "Downville Station",
        "Delta Crossing",
        "Jackville Terminal",
        "Passing Station")
    } st.addItem(value)
    station.foreach(_.selectItemByIndex(0))

    for {
      lev <- level
      value <- List(
        "Facile",
        "Medio", "Difficile",
        "Personalizzato")
    } lev.addItem(value)
    level.map(_.selectItemByIndex(0))

    for {
      dur <- duration
      s <- List(
        "Corto (5 min.)",
        "Medio (15 min.)",
        "Lungo (30 min.)",
        "Personalizzato")
    } dur.addItem(s)
    duration.map(_.selectItemByIndex(0))
  }

  /**
   *
   */
  private def optParameters: Option[GameParameters] =
    for {
      s <- station
      l <- level
      d <- duration
      a <- autoLock
      m <- mute
      v <- volume
    } yield GameParameters(
      stationName = s.getSelection,
      levelName = l.getSelection,
      durationName = d.getSelection,
      trainFrequence = FrequenceEnum.valueById(l.getSelectedIndex),
      duration = DurationEnum.valueById(d.getSelectedIndex),
      autoLock = a.isChecked,
      mute = m.isChecked,
      volume = v.getValue / 100)

  /**
   *
   */
  def parameters: GameParameters = optParameters.getOrElse(DefaultParms)
}
