/**
 *
 */
package org.mmarini.scala.jmonkey

import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.controls.NiftyControl
import de.lessvoid.nifty.elements.Element
import rx.lang.scala.Subject
import rx.lang.scala.Observable
import de.lessvoid.nifty.NiftyEventSubscriber
import de.lessvoid.nifty.controls.ButtonClickedEvent

/**
 * @author us00852
 *
 */
class ScreenControllerAdapter extends ScreenController {

  var niftyOpt: Option[Nifty] = None
  var screenOpt: Option[Screen] = None
  val screenObs = Subject[(String, ScreenControllerAdapter)]()

  /**   */
  override def bind(nifty: Nifty, screen: Screen) {
    niftyOpt = Some(nifty)
    screenOpt = Some(screen)
    for (s <- screenOpt) screenObs.onNext("bind", this)
  }

  /**  */
  def onStartScreen {
    for (s <- screenOpt) screenObs.onNext("start", this)
  }

  /** */
  def onEndScreen {
    for (s <- screenOpt) screenObs.onNext("end", this)
  }
}
