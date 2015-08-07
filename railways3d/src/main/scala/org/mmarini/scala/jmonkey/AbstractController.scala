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

/**
 * @author us00852
 *
 */
trait AbstractController extends ScreenController with ScreenUtil with NiftyUtil {

  private val _screenObservable = Subject[String]()

  def screenObservable: Observable[String] = _screenObservable

  /**   */
  override def bind(nifty: Nifty, screen: Screen) {
    this.nifty = Some(nifty)
    this.screen = Some(screen)
  }

  /**  */
  override def onStartScreen {
    for (s <- screen) _screenObservable.onNext(("start"))
  }

  /** */
  override def onEndScreen {
    for (s <- screen) _screenObservable.onNext(("end"))
  }
}
