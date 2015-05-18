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

  private def station = screen.map(_.findNiftyControl("station", classOf[DropDown[String]]))

  private def level = screen.map(_.findNiftyControl("level", classOf[DropDown[String]]))

  private def duration = screen.map(_.findNiftyControl("duration", classOf[DropDown[String]]))

  private def autoLock = screen.map(_.findNiftyControl("autoLock", classOf[CheckBox]))

  private def mute = screen.map(_.findNiftyControl("mute", classOf[CheckBox]))

  private def volume = screen.map(_.findNiftyControl("volume", classOf[Slider]))

  private val _confirmed = Subject[String]()

  private val DefaultParms = GameParameters(
    "Delta Crossing",
    "Facile",
    "Corto (5 min.)",
    FrequenceEnum.valueById(0),
    DurationEnum.valueById(0),
    true,
    false,
    0.5f)

  def confirmed: Observable[String] = _confirmed

  /**
   *
   */
  override def bind(nifty: Nifty, screen: Screen) {
    super.bind(nifty, screen)

    for {
      st <- station
      value <- List(
        "Delta Crossing",
        "Downville Station",
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
  def okPressed {
    _confirmed.onNext("confirmed")
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
      s.getSelection,
      l.getSelection,
      d.getSelection,
      FrequenceEnum.valueById(l.getSelectedIndex),
      DurationEnum.valueById(d.getSelectedIndex),
      a.isChecked,
      m.isChecked,
      v.getValue / 100)

  /**
   *
   */
  def parameters: GameParameters = optParameters.getOrElse(DefaultParms)
}
