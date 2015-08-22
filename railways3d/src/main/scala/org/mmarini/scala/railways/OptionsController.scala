/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.ButtonClickedObservable
import org.mmarini.scala.railways.model.GameParameters
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.DropDown
import de.lessvoid.nifty.controls.CheckBox
import de.lessvoid.nifty.controls.Slider
import de.lessvoid.nifty.screen.Screen
import rx.lang.scala.Subject
import org.mmarini.scala.jmonkey.ScreenControllerAdapter
import de.lessvoid.nifty.screen.ScreenController
import org.mmarini.scala.jmonkey.ScreenAdapter

/**
 * @author us00852
 *
 */
class OptionsController extends ScreenControllerAdapter
    with ScreenAdapter
    with ButtonClickedObservable
    with LazyLogging {

  val FrequenceEnum = new Enumeration {
    val Easy, Medium, Difficult, Custom = Value
    val valueById = Map(Easy.id -> 20 / 3600f, Medium.id -> 50 / 3600f, Difficult.id -> 100 / 3600f)
  }

  val DurationEnum = new Enumeration {
    val Short, Medium, Long, Custom = Value
    val valueById = Map(Short.id -> 5 * 60f, Medium.id -> 10 * 60f, Long.id -> 30 * 6f)
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
