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
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.elements.render.ElementRenderer
import de.lessvoid.nifty.controls.Controller
import org.mmarini.scala.railways.ObservableFactory
import rx.lang.scala.subjects.AsyncSubject

/**
 * @author us00852
 *
 */
class ScreenControllerAdapter extends ScreenController with LazyLogging {

  private val bindObs = AsyncSubject[(Nifty, Screen)]()

  def niftyObs: Observable[Nifty] = for (x <- bindObs) yield x._1

  def screenObs: Observable[Screen] = for (x <- bindObs) yield x._2

  val screenEventObs = Subject[(String, ScreenControllerAdapter)]()

  //  logger.debug("Screen controller created")

  /**   */
  override def bind(nifty: Nifty, screen: Screen) {
    //    logger.debug("Screen controller bound to {}", screen)
    screenEventObs.onNext("bind", this)
    bindObs.onNext((nifty, screen))
    bindObs.onCompleted()
  }

  /**  */
  override def onStartScreen {
    //    logger.debug("Screen controller {} on start screen", this)
    screenEventObs.onNext("start", this)
  }

  /** */
  override def onEndScreen {
    //    logger.debug("Screen controller {} on end screen", this)
    screenEventObs.onNext("end", this)
  }
}
