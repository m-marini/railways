/**
 *
 */
package org.mmarini.scala.railways

import scala.IndexedSeq

import org.mmarini.scala.jmonkey.ControllerAdapter
import org.mmarini.scala.jmonkey.TableController

import com.typesafe.scalalogging.LazyLogging

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class MessageController extends ControllerAdapter
    with TableController
    with LazyLogging {

  override def cellStyle = (_, _) => "text.message"

  /** Shows the camera views in the camera list panel */
  def show(msgs: Seq[String]) {
    setCells(for (m <- msgs.toIndexedSeq) yield IndexedSeq(m))
  }
}
