package org.mmarini.scala.jmonkey

import scala.util.Try

import org.mmarini.scala.railways.OptionObservableFactory

import com.jme3.math.Vector2f
import com.typesafe.scalalogging.LazyLogging

import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.elements.Element
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.tools.SizeValue
import rx.lang.scala.Observable
import rx.lang.scala.Subscription

/**
 * @author us00852
 */
trait NiftyObservables extends LazyLogging {

  def niftyObs: Observable[Nifty]

  def gotoScreen(id: String): Subscription = niftyObs.subscribe(nifty => nifty.gotoScreen(id))

  /** */
  def screenByIdObs(id: String): Observable[Screen] = {
    val obs = for { n <- niftyObs } yield Option(n.getScreen(id))
    obs.optionFlatten
  }

  /** */
  def screenControllerByIdObs[T <: ScreenController](id: String): Observable[T] = {
    val obs = for { scr <- screenByIdObs(id) } yield Option(scr.getScreenController().asInstanceOf[T])
    obs.optionFlatten
  }

  /** Create a popup instance */
  def createPopupObs(id: String): Observable[Try[Element]] = {
    val opt = for {
      n <- niftyObs
    } yield {
      val popupTry = Try {
        n.createPopup(id)
      }
      for (ex <- popupTry.failed) { logger.error(ex.getMessage, ex) }
      popupTry
    }
    opt
  }

  /**
   * Subscribes for popup show
   *
   * @param ctx observable with popup element, panelId and location
   */
  def showPopupAt(popup: Element, panelId: String, location: Vector2f): Subscription =
    niftyObs.subscribe(nifty =>
      for { pane <- Option(popup.findElementByName(panelId)) } {
        val id = popup.getId()
        pane.setConstraintX(SizeValue.px(location.getX.toInt))
        pane.setConstraintY(SizeValue.px(popup.getHeight - location.getY.toInt))
        nifty.showPopup(nifty.getCurrentScreen, id, null)
      })
}