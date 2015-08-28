/**
 *
 */
package org.mmarini.scala.jmonkey

import org.mmarini.scala.railways.OptionObservableFactory

import de.lessvoid.nifty.controls.Controller
import de.lessvoid.nifty.controls.NiftyControl
import de.lessvoid.nifty.elements.Element
import de.lessvoid.nifty.elements.render.ElementRenderer
import de.lessvoid.nifty.screen.Screen
import rx.lang.scala.Observable

/**
 * @author us00852
 */
trait ScreenObservables {

  def screenObs: Observable[Screen]

  /** Returns a nifty control observable*/
  def controlByIdObs[T <: NiftyControl](id: String, clazz: Class[T]): Observable[T] =
    for (Some(ctrl) <- screenObs.map(_.controlByIdOpt(id, clazz))) yield ctrl

  /** Returns a nifty element observable */
  def elementByIdObs(id: String): Observable[Element] =
    for (Some(element) <- screenObs.map(_.elementByIdOpt(id))) yield element

  /** */
  def redererByIdObs[T <: ElementRenderer](id: String, clazz: Class[T]): Observable[T] =
    for (Some(renderer) <- screenObs.map(_.redererByIdOpt(id, clazz))) yield renderer

  /** */
  def controllerByIdObs[T <: Controller](id: String, clazz: Class[T]): Observable[T] =
    for (Some(ctrl) <- screenObs.map(_.controllerByIdOpt(id, clazz))) yield ctrl

}
