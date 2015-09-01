/**
 *
 */
package org.mmarini.scala.railways

import scala.IndexedSeq
import org.mmarini.scala.jmonkey.TableController
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.builder.ElementBuilder
import de.lessvoid.nifty.builder.ImageBuilder
import de.lessvoid.nifty.builder.TextBuilder
import de.lessvoid.nifty.elements.Element

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class TrainController extends TableController
    with LazyLogging {

  override val headerOpt = Some(IndexedSeq(
    "",
    "Train",
    "To",
    "At",
    "Km/h"))

  override def headerStyle: Int => String = (_) => "text.light-panel-head"

  override val columnStyle: Int => String = IndexedSeq(
    "panel.train-light",
    "panel.train-id",
    "panel.train-to",
    "panel.train-at",
    "panel.train-speed")

  override def cellStyle: (Int, Int) => String = (_, col) => col match {
    case 0 => "image.camera-selection"
    case 4 => "text.selectable-light-panel-right"
    case _ => "text.selectable-light-panel"
  }

  override def builder: Int => ElementBuilder = (col) => { if (col == 0) new ImageBuilder else new TextBuilder }

  override def setter: (Int, Int) => (Element, String) => Unit = (_, col) => { if (col == 0) styleSetter else textSetter }

}
