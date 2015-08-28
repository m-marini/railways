/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.jmonkey.ControllerAdapter
import org.mmarini.scala.railways.model.Train
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.builder.TextBuilder
import de.lessvoid.nifty.elements.render.TextRenderer
import org.mmarini.scala.jmonkey.TableController
import rx.lang.scala.Observable
import rx.lang.scala.Subscription

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class TrainController extends TableController
    with LazyLogging {

  override val headerOpt = Some(IndexedSeq(
    "Train",
    "To",
    "At",
    "Km/h"))

  override def headerStyle: Int => String = (_) => "text.light-panel-head"

  override val columnStyle: Int => String = IndexedSeq(
    "panel.train-id",
    "panel.train-to",
    "panel.train-at",
    "panel.train-speed")

  override def cellStyle: (Int, Int) => String = (_, col) => if (col == 3) {
    "text.selectable-light-panel-right"
  } else {
    "text.selectable-light-panel"
  }

}
