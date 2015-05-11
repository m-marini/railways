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
trait AbstractController extends ScreenController {

  private var _nifty: Option[Nifty] = None
  private var _screen: Option[Screen] = None

  def nifty: Nifty = _nifty.get
  def screen: Screen = _screen.get

  /**
   *
   */
  def bind(nifty: Nifty, screen: Screen) {
    _nifty = Some(nifty)
    _screen = Some(screen)
  }

  /**
   *
   */
  def onStartScreen() {
  }

  /**
   *
   */
  def onEndScreen() {
  }
}
