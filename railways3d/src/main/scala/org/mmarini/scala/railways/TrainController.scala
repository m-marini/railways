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
class TrainController extends ControllerAdapter
    with TableController
    with LazyLogging {

  override val headerOpt = Some(IndexedSeq(
    "Train",
    "To",
    "At",
    "Km/h"))

  override def headerStyle = (_) => "text.light-panel-head"

  override val columnStyle = IndexedSeq(
    "panel.train-id",
    "panel.train-to",
    "panel.train-at",
    "panel.train-speed")

  override def cellStyle = (_, col) => if (col == 3)
    "text.selectable-light-panel-right"
  else
    "text.selectable-light-panel"

  /** Shows the camera views in the camera list panel */
  private def show(trains: Seq[Train]) {
    val cells = for { t <- trains.toIndexedSeq }
      yield IndexedSeq(
      t.id.toUpperCase,
      t.exitId.toUpperCase,
      "---",
      f"${(t.speed * 3.6).toInt}%d")

    setCells(cells)
  }

  /** Subscribes for train status changes */
  def subscribe(trainsObs: Observable[Seq[Train]]): Subscription =
    trainsObs.subscribe(x => show _)

}
