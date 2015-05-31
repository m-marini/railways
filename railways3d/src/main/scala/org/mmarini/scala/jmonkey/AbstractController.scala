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
trait AbstractController extends ScreenController {

  private var _nifty: Option[Nifty] = None
  private var _screen: Option[Screen] = None
  private val _screenObservable = Subject[String]()

  def screenObservable: Observable[String] = _screenObservable

  def nifty: Option[Nifty] = _nifty

  def screen: Option[Screen] = _screen

  /** Returns a nifty control */
  def control[T <: NiftyControl](id: String, cl: Class[T]): Option[T] = screen.map(_.findNiftyControl(id, cl))

  /** Returns a nifty element */
  def element(id: String): Option[Element] = screen.map(_.findElementByName(id))

  /**   */
  def bind(nifty: Nifty, screen: Screen) {
    _nifty = Some(nifty)
    _screen = Some(screen)
  }

  /**  */
  override def onStartScreen() {
    for (s <- screen) _screenObservable.onNext(("start"))
  }

  /** */
  override def onEndScreen() {
    for (s <- screen) _screenObservable.onNext(("end"))
  }
}
