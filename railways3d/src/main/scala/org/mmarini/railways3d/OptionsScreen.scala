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

/**
 * @author us00852
 *
 */
class OptionsScreen extends AbstractAppState with ScreenController {
  private val DefaultVolume = 50f
  private val frequenceEnum = new Enumeration {
    val Easy, Medium, Difficult, Custom = Value
    val valueById = Map(Easy -> 10, Medium -> 20, Difficult -> 60).map { case (k, v) => (k.id -> v.toFloat / 60) }
  }
  private val durationEnum = new Enumeration {
    val Short, Medium, Long, Custom = Value
    val valueById = Map(Short -> 5, Medium -> 10, Long -> 30).map { case (k, v) => (k.id -> v.toFloat * 60) }
  }

  private var nifty: Option[Nifty] = None
  private var station: Option[DropDown[String]] = None
  private var level: Option[DropDown[String]] = None
  private var duration: Option[DropDown[String]] = None
  private var autoLock: Option[CheckBox] = None
  private var mute: Option[CheckBox] = None
  private var volume: Option[Slider] = None

  /**
   *
   */
  def bind(nifty: Nifty, screen: Screen) {
    this.nifty = Some(nifty)

    station = Some(screen.findNiftyControl("station", classOf[DropDown[String]]))
    level = Some(screen.findNiftyControl("level", classOf[DropDown[String]]))
    duration = Some(screen.findNiftyControl("duration", classOf[DropDown[String]]))
    autoLock = Some(screen.findNiftyControl("autoLock", classOf[CheckBox]))
    mute = Some(screen.findNiftyControl("mute", classOf[CheckBox]))
    volume = Some(screen.findNiftyControl("volume", classOf[Slider]))

    station.map(s => {
      s.addItem("Delta Crossing")
      s.addItem("Downville Station")
      s.addItem("Jackville Terminal")
      s.addItem("Passing Station")
      s.selectItemByIndex(0)
    })

    level.map(l => {
      l.addItem("Facile")
      l.addItem("Medio")
      l.addItem("Difficile")
      l.addItem("Personalizzato")
      l.selectItemByIndex(0)
    })

    duration.map(d => {
      d.addItem("Corto (5 min.)")
      d.addItem("Medio (15 min.)")
      d.addItem("Lungo (30 min.)")
      d.addItem("Personalizzato")
      d.selectItemByIndex(0)
    })
  }

  /**
   *
   */
  def parameters: GameParameters = {
    GameParameters(
      station.map(_.getSelection()).getOrElse("???"),
      level.map(_.getSelection()).getOrElse("???"),
      duration.map(_.getSelection()).getOrElse("???"),
      frequenceEnum.valueById(level.map(_.getSelectedIndex()).getOrElse(0)),
      durationEnum.valueById(duration.map(_.getSelectedIndex()).getOrElse(0)),
      autoLock.map(_.isChecked()).getOrElse(true),
      mute.map(_.isChecked()).getOrElse(false),
      volume.map(_.getValue() / 100).getOrElse(0.5f))
  }

  /**
   *
   */
  def onStartScreen() {
    autoLock.foreach(_.check())
    volume.foreach(_.setValue(DefaultVolume))
  }

  /**
   *
   */
  def onEndScreen() {
  }
}
