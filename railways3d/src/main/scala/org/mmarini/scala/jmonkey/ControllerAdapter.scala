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
import org.mmarini.scala.railways.ObservableFactory

/**
 * @author us00852
 */
class ControllerAdapter extends Controller with LazyLogging {

  private val bindObs = Subject[(Nifty, Screen, Element)]()

  private val cacheObs = bindObs.latest

  def niftyObs: Observable[Nifty] = for { x <- cacheObs } yield x._1

  def screenObs: Observable[Screen] = for { x <- cacheObs } yield x._2

  def elementObs: Observable[Element] = for { x <- cacheObs } yield x._3

  val controllerEventObs = Subject[(String, ControllerAdapter)]()

  //  logger.debug("Controller created")

  /** Binds this pop up */
  override def bind(n: Nifty, s: Screen, e: Element, p: Properties, a: Attributes) {
    bindObs.onNext(n, s, e)
    bindObs.onCompleted()
    controllerEventObs.onNext("bind", this)
  }

  /** Initializes this pop up */
  override def init(p: Properties, a: Attributes) {
    controllerEventObs.onNext("init", this)
  }

  override def onStartScreen {
    controllerEventObs.onNext("start", this)
  }

  override def onFocus(f: Boolean) {
    controllerEventObs.onNext("focus", this)
  }

  override def inputEvent(ev: NiftyInputEvent): Boolean = {
    logger.debug("Controller {} input event {}", this, ev)
    false
  }
}