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
import org.mmarini.scala.jmonkey.ScreenObservables
import rx.lang.scala.Observable

/**
 * @author us00852
 *
 */
object OptionsController {

  val FrequenceEnum = new Enumeration {
    val Easy, Medium, Difficult, Custom = Value
    val valueById = Map(Easy.id -> 20 / 3600f, Medium.id -> 50 / 3600f, Difficult.id -> 100 / 3600f)
  }

  val DurationEnum = new Enumeration {
    val Short, Medium, Long, Custom = Value
    val valueById = Map(Short.id -> 5 * 60f, Medium.id -> 10 * 60f, Long.id -> 30 * 6f)
  }

  val DefaultParms = GameParameters(
    "Downville Station",
    "Facile",
    "Corto (5 min.)",
    FrequenceEnum.valueById(0),
    DurationEnum.valueById(0),
    //    10,
    true,
    false,
    0.5f)
}

class OptionsController extends ScreenControllerAdapter
    with ScreenObservables
    with ButtonClickedObservable
    with LazyLogging {

  import OptionsController._

  private def stationObs = controlByIdObs("station", classOf[DropDown[String]])

  private def levelObs = controlByIdObs("level", classOf[DropDown[String]])

  private def durationObs = controlByIdObs("duration", classOf[DropDown[String]])

  private def autoLockObs = controlByIdObs("autoLock", classOf[CheckBox])

  private def muteObs = controlByIdObs("mute", classOf[CheckBox])

  private def volumeObs = controlByIdObs("volume", classOf[Slider])

  stationObs.subscribe(ctrl => {
    for {
      value <- List(
        "Downville Station",
        "Delta Crossing",
        "Jackville Terminal",
        "Passing Station")
    } ctrl.addItem(value)
    ctrl.selectItemByIndex(0)
  })

  levelObs.subscribe(ctrl => {
    for {
      value <- List(
        "Facile",
        "Medio", "Difficile",
        "Personalizzato")
    } ctrl.addItem(value)
    ctrl.selectItemByIndex(0)
  })

  durationObs.subscribe(ctrl => {
    for {
      value <- List(
        "Corto (5 min.)",
        "Medio (15 min.)",
        "Lungo (30 min.)",
        "Personalizzato")
    } ctrl.addItem(value)
    ctrl.selectItemByIndex(0)
  })

  /**
   *
   */
  def readParametersObs: Observable[GameParameters] =
    for {
      s <- stationObs
      l <- levelObs
      d <- durationObs
      a <- autoLockObs
      m <- muteObs
      v <- volumeObs
    } yield GameParameters(
      stationName = s.getSelection,
      levelName = l.getSelection,
      durationName = d.getSelection,
      trainFrequence = FrequenceEnum.valueById(l.getSelectedIndex),
      duration = DurationEnum.valueById(d.getSelectedIndex),
      autoLock = a.isChecked,
      mute = m.isChecked,
      volume = v.getValue / 100)
}

