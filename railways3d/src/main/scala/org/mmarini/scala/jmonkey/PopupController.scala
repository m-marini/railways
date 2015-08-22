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
class PopupController extends ControllerAdapter
    with MousePrimaryClickedObservable {

  mousePrimaryClickedObs.subscribe(_ => closePopup)

  /** Close this popup */
  def closePopup {
    for {
      nifty <- niftyOpt
      popup <- elementOpt
    } {
      nifty.closePopup(popup.getId)
    }
  }

}