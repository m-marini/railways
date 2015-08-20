package org.mmarini.scala.jmonkey

import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.Nifty
import scala.util.Try
import de.lessvoid.nifty.elements.Element
import com.jme3.math.Vector2f
import de.lessvoid.nifty.tools.SizeValue

/**
 * @author us00852
 */
trait NiftyUtil extends LazyLogging {
  var niftyOpt: Option[Nifty] = None

  /** */
  def screenById = (id: String) =>
    for {
      n <- niftyOpt
      s <- Option(n.getScreen(id))
    } yield s

  /** */
  def screenControllerById[T]: String => Option[T] = (id) =>
    for {
      scr <- screenById(id)
      ctrl <- Option(scr.getScreenController().asInstanceOf[T])
    } yield ctrl

  /** Create a popup instance */
  def createPopup: String => Option[Element] = (id) =>
    for (n <- niftyOpt) yield {
      val popupTry = Try {
        n.createPopup(id)
      }
      for (ex <- popupTry.failed) {
        logger.error(ex.getMessage, ex)
      }
      popupTry.getOrElse(null)
    }

  /** **/
  def showPopupAt: (Element, String, Vector2f) => Unit = (popup, paneId, pos) =>
    for {
      n <- niftyOpt
      pane <- Option(popup.findElementByName(paneId))
    } {
      val id = popup.getId()
      pane.setConstraintX(SizeValue.px(pos.getX.toInt))
      pane.setConstraintY(SizeValue.px(popup.getHeight - pos.getY.toInt))
      n.showPopup(n.getCurrentScreen, popup.getId, null)
    }

}