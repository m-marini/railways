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
import org.mmarini.scala.jmonkey.ScreenObservable
import org.mmarini.scala.jmonkey.AbstractScreenController
import org.mmarini.scala.jmonkey.MousePrimarClickedObservable
import org.mmarini.scala.jmonkey.MousePrimarReleaseObservable
import org.mmarini.scala.jmonkey.ListBoxSelectionChangedObservable
import rx.lang.scala.Observable
import org.mmarini.scala.railways.model.TrainMessage
import org.mmarini.scala.railways.model.Train
import org.mmarini.scala.railways.model.MovingTrain
import org.mmarini.scala.railways.model.StoppingTrain
import org.mmarini.scala.railways.model.StoppedTrain
import org.mmarini.scala.railways.model.WaitForPassengerTrain
import org.mmarini.scala.railways.model.WaitingForTrackTrain
import org.mmarini.scala.railways.model.GamePerformance
import de.lessvoid.nifty.elements.render.TextRenderer
import org.mmarini.scala.jmonkey.JmeController
import org.mmarini.scala.jmonkey.NiftyUtil
import org.mmarini.scala.jmonkey.ScreenUtil
import collection.JavaConversions._
import de.lessvoid.nifty.builder.TextBuilder
import de.lessvoid.nifty.builder.ImageBuilder
import scala.math.min
import scala.math.max

/**
 * Controls the game screen
 *
 * It exposes a game start observer that creates a game for each event
 * The generated game handles the user event and clocks tick updating the rootNode of application.
 */
class MessageController extends JmeController
    with ColumnController
    with LazyLogging {

  var log = Seq[String]()

  /** Shows the camera views in the camera list panel */
  def show(msgs: Seq[String]) {
    log = (msgs ++ log) take 3
    val tb = new TextBuilder
    tb.style("text.message")
    showColumn[String]("message-col",
      (text) => {
        tb.text(text)
        tb
      },
      (elem, text) => elem.getRenderer(classOf[TextRenderer]).setText(text),
      log)
  }
}
