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
trait DefaultScreenController extends AbstractScreenController with ScreenUtil with NiftyUtil {
  /**   */
  override def bind(nifty: Nifty, screen: Screen) {
    this.nifty = Some(nifty)
    this.screen = Some(screen)
  }

  override def onStartScreen {}

  override def onEndScreen {}
}
