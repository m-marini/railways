/**
 *
 */
package org.mmarini.scala.railways

import org.mmarini.scala.railways.model.GameParameters
import com.jme3.app.state.AbstractAppState
import com.typesafe.scalalogging.LazyLogging
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.ListBox
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent
import de.lessvoid.nifty.screen.Screen
import rx.lang.scala.Observer
import rx.lang.scala.Subject
import de.lessvoid.nifty.NiftyEventSubscriber
import scala.util.Try
import de.lessvoid.nifty.elements.Element
import com.jme3.math.Vector2f
import org.mmarini.scala.jmonkey.TableController
import de.lessvoid.nifty.builder.ImageBuilder
import de.lessvoid.nifty.builder.TextBuilder
import org.mmarini.scala.jmonkey.ScreenObservable
import org.mmarini.scala.jmonkey.JmeController

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class CameraController extends JmeController
    with TableController
    with LazyLogging {

  override def cellStyle = (_, col) => if (col == 0) "image.camera-selection" else "text.camera-name"

  override def setter = (_, col) => { if (col == 0) dummySetter else textSetter }

  override def builder = (col) => { if (col == 0) new ImageBuilder else new TextBuilder }

  /** Shows the camera views in the camera list panel */
  def showCameras(cams: Seq[String]) {
    val s = cams.toIndexedSeq
    val xx = for { n <- s } yield IndexedSeq("", n)
    setCells(xx)
  }
}
