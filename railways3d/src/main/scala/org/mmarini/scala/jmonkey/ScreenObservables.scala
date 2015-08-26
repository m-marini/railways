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
  def controlByIdObs[T <: NiftyControl](id: String, clazz: Class[T]): Observable[T] = {
    val obs = for { screen <- screenObs } yield Option(screen.findNiftyControl(id, clazz))
    obs.optionFlatten
  }

  /** Returns a nifty element observable */
  def elementByIdObs(id: String): Observable[Element] = {
    val obs = for { s <- screenObs } yield Option(s.findElementByName(id))
    obs.optionFlatten
  }

  /** */
  def redererByIdObs[T <: ElementRenderer](id: String, clazz: Class[T]): Observable[T] =
    for { el <- elementByIdObs(id) } yield el.getRenderer(clazz)

  /** */
  def controllerByIdObs[T <: Controller](id: String, clazz: Class[T]): Observable[T] =
    for { el <- elementByIdObs(id) } yield el.getControl(clazz)

}