package org.mmarini.scala.jmonkey

import de.lessvoid.nifty.controls.Controller
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.elements.Element
import java.util.Properties
import de.lessvoid.xml.xpp3.Attributes
import de.lessvoid.nifty.input.NiftyInputEvent
import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala.Subject
import rx.lang.scala.Observable
import de.lessvoid.nifty.controls.NiftyControl

/**
 * @author us00852
 */
class PopupController extends Controller
    with ScreenUtil
    with NiftyUtil
    with MousePrimarClickedObservable {

  var popup: Option[Element] = None

  mousePrimaryClickedObs.subscribe(_ => closePopup)

  /** Binds this pop up */
  override def bind(n: Nifty, s: Screen, e: Element, p: Properties, a: Attributes) {
    this.nifty = Some(n)
    this.screen = Some(s)
    popup = Some(e)
  }

  /** Initializes this pop up */
  override def init(p: Properties, a: Attributes) {}

  override def onStartScreen {
  }

  override def onFocus(f: Boolean) {}

  override def inputEvent(ev: NiftyInputEvent): Boolean = false

  /** Close this popup */
  def closePopup {
    for {
      nifty <- nifty
      popup <- popup
    } {
      nifty.closePopup(popup.getId)
    }
  }

}