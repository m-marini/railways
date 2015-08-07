package org.mmarini.scala.jmonkey

import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.controls.NiftyControl
import de.lessvoid.nifty.elements.Element
import de.lessvoid.nifty.elements.render.ElementRenderer

/**
 * @author us00852
 */
trait ScreenUtil {

  var screen: Option[Screen] = None

  /** Returns a nifty control */
  def controlById[T <: NiftyControl]: (String, Class[T]) => Option[T] = (id, clazz) =>
    for {
      s <- screen
      ctrl <- Option(s.findNiftyControl(id, clazz))
    } yield ctrl

  /** Returns a nifty element */
  def elementById: String => Option[Element] = (id) =>
    for {
      s <- screen
      el <- Option(s.findElementByName(id))
    } yield el

  /** */
  def redererById[T <: ElementRenderer]: (String, Class[T]) => Option[T] = (id, clazz) =>
    for {
      el <- elementById(id)
      r <- Option(el.getRenderer(clazz))
    } yield r
}