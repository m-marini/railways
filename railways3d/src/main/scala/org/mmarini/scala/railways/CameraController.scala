/**
 *
 */
package org.mmarini.scala.railways

import scala.IndexedSeq

import org.mmarini.scala.jmonkey.ControllerAdapter
import org.mmarini.scala.jmonkey.TableController

import com.typesafe.scalalogging.LazyLogging

import de.lessvoid.nifty.builder.ImageBuilder
import de.lessvoid.nifty.builder.TextBuilder
import rx.lang.scala.Observable

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class CameraController extends TableController
    with LazyLogging {

  override def cellStyle = (_, col) => if (col == 0) "image.camera-selection" else "text.camera-name"

  override def setter = (_, col) => { if (col == 0) dummySetter else textSetter }

  override def builder = (col) => { if (col == 0) new ImageBuilder else new TextBuilder }
}
