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

/**
 * @author us00852
 *
 */
class ScreenControllerAdapter extends ScreenController with LazyLogging {

  val bindObs = Subject[(Nifty, Screen)]()

  private val cache = bindObs.first.cache(1)

  cache.subscribe()

  def niftyObs: Observable[Nifty] = for (x <- cache) yield x._1

  def screenObs: Observable[Screen] = for (x <- cache) yield x._2

  val screenEventObs = Subject[(String, ScreenControllerAdapter)]()

  logger.debug("Screen controller created")

  /**   */
  override def bind(nifty: Nifty, screen: Screen) {
    screenEventObs.onNext("bind", this)
    bindObs.onNext((nifty, screen))
    bindObs.onCompleted()
    logger.debug("Screen controller bound to {}", screen)
  }

  /**  */
  override def onStartScreen {
    logger.debug("Screen controller {} on start screen", this)
    screenEventObs.onNext("start", this)
  }

  /** */
  override def onEndScreen {
    logger.debug("Screen controller {} on end screen", this)
    screenEventObs.onNext("end", this)
  }
}
