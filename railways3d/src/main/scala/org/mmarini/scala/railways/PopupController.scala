package org.mmarini.scala.railways

import de.lessvoid.nifty.controls.Controller
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.screen.ScreenController
import org.mmarini.scala.jmonkey.AbstractController
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.elements.Element
import java.util.Properties
import de.lessvoid.xml.xpp3.Attributes
import de.lessvoid.nifty.input.NiftyInputEvent
import com.typesafe.scalalogging.LazyLogging

/**
 * @author us00852
 */
class PopupController extends Controller with LazyLogging {

  private var nifty: Option[Nifty] = None
  private var screen: Option[Screen] = None
  private var element: Option[Element] = None

  override def bind(n: Nifty, s: Screen, e: Element, p: Properties, a: Attributes) {
    nifty = Some(n)
    screen = Some(s)
    element = Some(e)
  }

  override def init(p: Properties, a: Attributes) {}

  override def onStartScreen() {}

  override def onFocus(f: Boolean) {}

  override def inputEvent(ev: NiftyInputEvent): Boolean = false

  def closePopup() {
    for {
      nifty <- nifty
      element <- element
    } {
      nifty.closePopup(element.getId)
    }
  }

  def doSomething() {
    logger.debug(s"$screen $element")
    closePopup()
  }

}